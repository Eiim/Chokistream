/**
 * NTRClient.java
 * Copyright (C) 2024  Eiim, ChainSwordCS, Herronjo
 * 
 * Some code is based on:
 * NTRClient, Copyright (C) 2016  Cell9 / 44670
 * NTR, Copyright (C) 2017  Cell9 / 44670
 * 
 * This file is licensed under GPLv2
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Todo (low-priority): Currently, this variable isn't 
	 * critical to functionality, and it's used in kind of a dumb way.
	 * Reimplement the functionality this is used for in a better way,
	 * and preferably in a way that doesn't rely on
	 * only one instance of NTRClient running at a time.
	 */
	public static AtomicBoolean instanceIsRunning = new AtomicBoolean(false);
	
	/** Thread used by NTRClient to read and buffer Frames received from the 3DS. */
	private final NTRUDPThread udpThread;
	/** Thread that keeps a persistent TCP connection to NTR. */
	private final HeartbeatThread hbThread;
	
	private final Random random = new Random();
	
	private static final Logger logger = Logger.INSTANCE;
	
	private int topFrames;
	private int bottomFrames;
	
	private Socket soc = null;
	private OutputStream socOut = null;
	private InputStream socIn = null;
	
	private String host = "";
	
	/**
	 * Todo (low-priority): Adjust code so it doesn't rely on
	 * only one instance of NTRClient running at a time.
	 */
	private static class SettingsChangeQueue {
		public boolean queued = false;
		// safe defaults just in case
		public int quality = 70;
		public DSScreen screen = DSScreen.TOP;
		public int priority = 4;
		public int qos = 16;
		public SettingsChangeQueue() {}
	}
	private static SettingsChangeQueue scq = new SettingsChangeQueue();
	private static NFCPatchType nfcPatchQueued = null;
	private int qualityDeltaQueue = 0;

	/**
	 * Create an NTRClient. 
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param screen Which screen gets priority.
	 * @param priority Priority factor.
	 * @param qos QoS value.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public NTRClient(String host, int quality, DSScreen screen, int priority, int qos, ColorMode colorMode, int port) throws Exception, UnknownHostException, IOException, InterruptedException {
		instanceIsRunning.set(true);
		scq.queued = false;
		udpThread = new NTRUDPThread(screen, colorMode, port);
		udpThread.start();
		hbThread = new HeartbeatThread();
		logger.log("Connecting...");
		
		this.host = host;
		if(udpThread.isReceivingFrames()) {
			/** 
			 * If the UDP Thread is receiving incoming frames
			 * before we even *connected* over TCP,
			 * then fast-start and defer initialization 
			 * of settings to HeartbeatThread.
			 * 
			 * Alternatively, in the case that the NFC Patch is fully active, 
			 * and in an applicable game such as Pokemon OR/AS,
			 * then the TCP connection will fail
			 * but NTR will still stream frames over UDP.
			 */
			scq.quality = quality;
			scq.screen = screen;
			scq.priority = priority;
			scq.qos = qos;
			scq.queued = true;
			hbThread.start();
		} else {
			try {
				reopenSocket();
				sendInitPacket(quality, screen, priority, qos);
				TimeUnit.SECONDS.sleep(2); // NTR may need some time to think
				hbThread.start();
			} catch (ConnectException e) {
				if(udpThread.isReceivingFrames()) {
					logger.log("NTRClient warning: "+e.getClass()+": "+e.getMessage());
					logger.log(Arrays.toString(e.getStackTrace()), LogLevel.VERBOSE);
					logger.log("NTR's NFC Patch seems to be active. Proceeding as normal...");
				} else {
					close();
					throw e;
				}
			} catch (Exception e) {
				close();
				throw e;
			}
		}
	}

	@Override
	public void close() {
		udpThread.interrupt();
		udpThread.close();
		if(hbThread != null) {
			hbThread.close();
			hbThread.interrupt();
		}
		if(soc != null && !soc.isClosed()) {
			try {
				soc.close();
			} catch (Exception e) {
				logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
			}
		}
		instanceIsRunning.set(false);
	}

	@Override
	public Frame getFrame() throws InterruptedException {
		Frame f = udpThread.getFrame();
		if(f.screen == DSScreen.TOP) {
			topFrames++;
		} else {
			bottomFrames++;
		}
		return f;
	}
	
	@Override
	public int framesSinceLast(DSScreenBoth screens) {
		switch(screens) {
			case TOP:
				int f = topFrames;
				topFrames = 0;
				return f;
			case BOTTOM:
				int f2 = bottomFrames;
				bottomFrames = 0;
				return f2;
			case BOTH:
				int f3 = topFrames + bottomFrames;
				topFrames = 0;
				bottomFrames = 0;
				return f3;
			default:
				return 0; // Should never happen
		}
	}
	
	/**
	 * TODO: minor edge-case behavior quirk:
	 * If the 3DS disconnects from wifi, then reconnects,
	 * without the Chokistream user having manually
	 * disconnected and reconnected:
	 * the NTRUDPThread starts processing frames again fine
	 * because it never stopped listening, but the
	 * HeartbeatThread stopped when the 3DS disconnected
	 * and makes no attempt to restart / reconnect.
	 * 
	 * Ideally, I'd want the HeartbeatThread to start up again,
	 * _somewhere_ in the code. Auto-reconnect is convenient.
	 * I'm open to alternate ideas for implementation.
	 * I haven't yet implemented a solution to cleanly handle this.
	 * 
	 * Additionally, note that this case is already handled
	 * more or less fine, it's just a minor quirk.
	 */

	/**
	 * Thread that keeps a persistent TCP connection to NTR.
	 */
	private class HeartbeatThread extends Thread {
		private boolean nfcPatchSent = false;
		public AtomicBoolean shouldDie = new AtomicBoolean(false);
		HeartbeatThread() {}
		
		public void close() {
			shouldDie.set(true);
		}
		
		@Override
		public void run() {
			if(soc == null || soc.isClosed()) {
				try {
					reopenSocket();
					scq.queued = true;
				} catch (Exception e) {
					boolean b = false;
					try {
						b = udpThread.isReceivingFrames();
					} catch (InterruptedException e2) {}
					if(b) {
						logger.log("NTR's NFC Patch seems to be active. Shutting down HeartbeatThread...");
						logger.log("NTRClient$HeartbeatThread warning: "+e.getClass()+": "+e.getMessage());
						logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
					} else {
						logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
					}
					shouldDie.set(true);
				}
			}
			
			while (!shouldDie.get()) {
				if(qualityDeltaQueue != 0) {
					int newQual = scq.quality + qualityDeltaQueue;
					qualityDeltaQueue = 0;
					if(newQual < 10) {
						newQual = 10;
					} else if(newQual > 100) {
						newQual = 100;
					}
					if(scq.quality != newQual) {
						scq.quality = newQual;
						scq.queued = true;
					}
				}

				if(scq.queued) {
					try {
						changeSettingsWhileRunning(scq.quality, scq.screen, scq.priority, scq.qos);
					} catch (Exception e) {
						logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
					}
					scq.queued = false;
				}
				
				try {
					heartbeat(); // TODO: use reply
				} catch (SocketException e) {
					/**
					 * "Connection reset" or "Connection reset by peer" (TODO: test for that string)
					 * Which usually means the NFC Patch is now fully active.
					 * NTR disconnected from Chokistream, and we can't reconnect over TCP.
					 * so kill this thread.
					 */
					logger.log("NTRClient$HeartbeatThread warning: "+e.getClass()+": "+e.getMessage());
					logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
					//if (nfcPatchSent)
					//logger.log("NTR's NFC Patch seems to be active. Shutting down HeartbeatThread...");
					shouldDie.set(true);
				} catch (SocketTimeoutException e) {
					logger.log("NTRClient$HeartbeatThread warning: "+e.getClass()+": "+e.getMessage());
					logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
				} catch (Exception e) {
					logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
				}
				
				if (!nfcPatchSent && nfcPatchQueued != null) {
					try {
						sendNFCPatch(nfcPatchQueued);
						nfcPatchSent = true;
					} catch (Exception e) {
						logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
					}
					nfcPatchQueued = null;
				}
				//TimeUnit.SECONDS.sleep(1);
			}
			scq.queued = false;
		}
	}
	
	/**
	 * (Re-) Opens the Socket at the specified IP address.
	 * 
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void reopenSocket() throws SocketException, UnknownHostException, IOException {
		if(soc != null && !soc.isClosed()) {
			soc.close();
		}
		
		try {
			/**
			 * TODO: Socket init takes waaaay too long. But I don't understand
			 * Java standard libraries enough to improve this yet.
			 * Specifically, the first line following this comment is the main hangup,
			 * Because we can't manually set a timeout period. And default is too long.
			 * I don't notice anything breaking due to this, but it's inconvenient. -C
			 */
			Socket newSoc = new Socket(host, 8000);
			soc = newSoc;
			soc.setSoTimeout(10000);
			socOut = soc.getOutputStream();
			socIn = soc.getInputStream();
		} catch (IOException e) {
			// TODO: Maybe close soc, for the sake of predictable behavior.
			throw e;
		}
	}
	
	/**
	 * NTR Heartbeat; gets any new available debug log output from NTR, and logs it via Chokistream.Logger.
	 * 
	 * <p>This function works with NTR 3.6, NTR 3.6.1, and NTR-HR.</p>
	 * 
	 * @return Debug output data received from NTR, converted to a UTF-8 String but otherwise unmodified.
	 *  If no debug output is received from NTR, this function returns an empty String.
	 * @throws Exception              Thrown when the supposed response from NTR is invalid.
	 *  Also rethrows any Exceptions thrown by {@link #recvPacket()}, and by extension {@link #Packet(byte[])}.
	 * @throws IOException            refer to {@link #sendPacket(Packet)} and {@link #recvPacket()}
	 */
	public String heartbeat() throws Exception, IOException {
		Packet pak = new Packet();
		int heartbeatSeq = random.nextInt(100);
		pak.seq = heartbeatSeq;
		pak.type = 0;
		pak.cmd = 0; // heartbeat command
		
		try {
			logger.log("Sending NTR Heartbeat packet...", LogLevel.EXTREME);
			sendPacket(pak);
			logger.log("NTR Heartbeat packet sent.", LogLevel.EXTREME);
		} catch(IOException e) {
			logger.log("NTR Heartbeat error: Packet failed to send.");
			throw e;
		}
		
		/*
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch(InterruptedException e) {
			// lol
		}
		*/
		
		Packet reply = recvPacket();
		
		/**
		 * Todo (low-priority):
		 * Theoretical issues with thread-safety;
		 * if we receive a reply packet that is replying to a 
		 * different command packet from the one sent above,
		 * (we can tell by the SEQ id number),
		 * then that reply packet is basically thrown-out.
		 * 
		 * However, this issue should basically never come up
		 * in practice, with the way things are designed right now.
		 * - ChainSwordCS
		 */
		if(reply.cmd != 0 || reply.seq != heartbeatSeq) {
			logger.log("NTR Heartbeat error: Received non-matching response packet.");
			if(reply.cmd != 0) {
				logger.log("cmd "+reply.cmd+" != 0");
			}
			if(reply.seq != heartbeatSeq) {
				logger.log("seq "+reply.seq+" != "+heartbeatSeq);
			}
			throw new Exception(); // TODO: More specific; add a message
		}
		
		logger.log("NTR Heartbeat response received.", LogLevel.EXTREME);
		
		// TODO: Split up logger output line by line, filter out known log spam.
		if(reply.exdata.length > 0) {
			String debugOutUnmodified = new String(reply.exdata, StandardCharsets.UTF_8);
			String debugOut = debugOutUnmodified;
			if(debugOut.charAt(debugOut.length()-1) == '\n') {
				debugOut = debugOut.substring(0, debugOut.length()-1);
			}
			// custom formatting
			String ntrText = null;
			if(debugOut.charAt(0) == '[') { // then it's most likely NTR-HR
				ntrText = "[NTR]";
			} else {
				ntrText = "[NTR] ";
			}
			debugOut = debugOut.replace("\n", "\n"+ntrText);
			logger.log(ntrText+debugOut, LogLevel.REGULAR);
			return debugOutUnmodified;
		} else {
			logger.log("NTR Heartbeat response is empty.", LogLevel.EXTREME);
			return "";
		}
	}
	
	/**
	 * Send a Reload command to NTR.
	 * <p>Not applicable to NTR-HR; in such a case,
	 *  the command will be ignored and this function should return safely.</p>
	 * 
	 * <p>Note: It is unknown whether NTR's Reload functionality works.</p>
	 * 
	 * @throws IOException refer to {@link #sendPacket(Packet)}
	 */
	public void sendReloadPacket() throws IOException {
		Packet pak = new Packet();
		pak.cmd = 4;
		try {
			logger.log("Sending Reload packet", LogLevel.VERBOSE);
			sendPacket(pak);
		} catch(IOException e) {
			logger.log("NTR Reload failed!");
			throw e;
		}
	}
	
	public void sendNFCPatch(NFCPatchType type) {
		Packet pak = new Packet();
		pak.seq = 24000;
		pak.type = 1;
		pak.cmd = 10;
		
		pak.args[0] = 26; // pid; 0x1A
		pak.args[1] = switch(type) {
			case OLD:
				yield 0x00105AE4; // Sys ver. < 11.4
			case NEW:
				yield 0x00105B00; // Sys ver. >= 11.4
		};
		
		pak.exdata = new byte[] {0x70,0x47};
		pak.args[2] = pak.exdata.length;
		
		try {
			sendPacket(pak);
			logger.log("NFC Patch sent!");
		} catch(IOException e) {
			logger.log(Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
			logger.log("NFC Patch failed to send");
		}
	}
	
	/**
	 * Sets the NFC Patch queue to apply a patch.
	 * If an NFC Patch is already in the queue, it is replaced.
	 * 
	 * <p>
	 *  Note regarding functionality:
	 *  Whether an instance of NTRClient is running or not,
	 *  this method is intended to work in both cases.
	 *  In either case, the requested NFC Patch will be sent
	 *  at the earliest convenience, when/if the NTRClient's
	 *  HeartbeatThread is up and running.
	 * </p>
	 * 
	 * @param ver Which version of the NFC Patch is to be used.
	 *            NFCPatchType.NEW = NFC Patch for System Update 11.4.x or higher
	 *            NFCPatchType.OLD = NFC Patch for System Update 11.3.x or lower
	 *            null = Empty the NFC Patch queue.
	 */
	public static void queueNFCPatch(NFCPatchType ver) {
		nfcPatchQueued = ver;
		if(ver == null && nfcPatchQueued != null) {
			logger.log("NTR NFC Patch un-queued");
		} else {
			// this check is unnecessary
			if(!instanceIsRunning.get()) {
				logger.log("NTR NFC Patch queued");
			}
		}
	}
	
	/**
	 * Sends a packet to NTR which configures settings and signals to start streaming image data.
	 * 
	 * <p>
	 *  Note: For NTR 3.6 and 3.6.1, if NTR has already started streaming,
	 *  these settings cannot be changed just by using this function.
	 *  This limitation does not apply to NTR-HR.
	 * </p>
	 * 
	 * @param quality
	 * @param screen
	 * @param priority
	 * @param qos NTR "Quality of Service" (misnomer)
	 * 
	 * @throws IOException refer to {@link #sendPacket(Packet)}
	 */
	public void sendInitPacket(int quality, DSScreen screen, int priority, int qos) throws IOException {
		Packet pak = new Packet();
		pak.seq = 3000;
		pak.type = 0;
		pak.cmd = 901;
		
		pak.args[0] = ((screen == DSScreen.TOP)? 1 : 0) << 8 | (priority % 256);
		pak.args[1] = quality;
		pak.args[2] = (qos*2) << 16; // Convert to the format expected by NTR and NTR-HR
		
		try {
			logger.log("Sending init packet", LogLevel.VERBOSE);
			sendPacket(pak);
		} catch(IOException e) {
			logger.log("Init packet failed to send");
			throw e;
		}
	}
	
	/**
	 * Try to change NTR video settings while NTR is already connected and running.
	 * For NTR-HR, this is essentially just a wrapper for sendInitPacket.
	 * 
	 * TODO: This doesn't yet work for NTR 3.6/3.6.1 :(
	 * The relevant code is currently commented-out,
	 * so it's still safe to call this method for NTR.
	 * 
	 * TODO: Make the Exception from heartbeat() more specific.
	 * 
	 * @throws IOException, InterruptedException, Exception
	 */
	public void changeSettingsWhileRunning(int quality, DSScreen screen, int priority, int qos) throws IOException, InterruptedException, Exception {
		try {
			sendInitPacket(quality, screen, priority, qos);
			
			// Give NTR some time to think
			TimeUnit.SECONDS.sleep(1);
			
			String heartbeatReply = heartbeat();
			
			/**
			 * NTR (3.6 or 3.6.1) needs to reload to reinitialize quality, priority screen, etc. (?)
			 * This is a somewhat hacky solution because a proper one doesn't exist.
			 * TODO: Account for the possible presence of irrelevant backlog debug output? (This *should* be harmless though.)
			 */
			if(heartbeatReply.contains("remote play already started")) {
				// TODO: This is commented-out because it doesn't work :(
				//logger.log("Reloading NTR...");
				//sendReloadPacket();
				//TimeUnit.SECONDS.sleep(3);
				//reopenSocket(host);
				//sendInitPacket(port, screen, priority, quality, qos);
				//TimeUnit.SECONDS.sleep(3);
				//heartbeat();
			}
		} catch (ConnectException e) {
			if(udpThread.isReceivingFrames()) {
				logger.log("NTRClient warning: "+e.getClass()+": "+e.getMessage());
				logger.log(Arrays.toString(e.getStackTrace()), LogLevel.VERBOSE);
				logger.log("NTR's NFC Patch seems to be active. Proceeding as normal...");
			} else {
				throw e;
			}
		}
	}
	
	/**
	 * Queues a Settings-Change command, which will be sent to NTR.
	 * 
	 * <p>
	 *  Note regarding functionality:
	 *  When an instance of NTRClient starts, the caller passes the desired settings.
	 *  So if NTRClient isn't running, there's no need to queue a settings-change.
	 *  This method is intended to be used only while an instance of NTRClient is already running.
	 *  If NTRClient isn't yet running, that gets handled gracefully by this method,
	 *  but this behavior may change in the future.
	 * </p>
	 */
	public static void queueSettingsChange(int quality, DSScreen screen, int priority, int qos) {
		// The queue system is robust enough to handle it gracefully, so this check is redundant (for now)
		if(instanceIsRunning.get()) {
			scq.quality = quality;
			scq.screen = screen;
			scq.priority = priority;
			scq.qos = qos;
			scq.queued = true;
		}
	}

	/**
	 * Increases or decreases video quality.
	 * 
	 * @param delta The amount by which to increase or decrease.
	 */
	public void incrementQuality(int delta) {
		qualityDeltaQueue += delta;
	}
	
	/**
	 * Sends a packet to NTR using this NTRClient's Socket soc.
	 * 
	 * @param packet the Packet to send.
	 * @throws IOException if an I/O error occurs.
	 */
	public void sendPacket(Packet packet) throws IOException {
		byte[] pak = packet.getRaw();
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		socOut.write(pak);
	}
	
	/**
	 * Receives a packet from NTR using this NTRClient's Socket soc.
	 * If an Exception is thrown, this function logs as much received data as it can, for debugging purposes.
	 * 
	 * @return the received Packet.
	 * @throws Exception if unable to receive a full 84-byte header, or if unable to receive full exdata section.
	 *  Also rethrows any Exceptions thrown by {@link #Packet(byte[])}.
	 * @throws IOException if an I/O error occurs.
	 */
	public Packet recvPacket() throws Exception, IOException {
		Exception exception = null;
		Packet pak = null;
		byte[] header = new byte[84];
		int bytesReadHeader = 0;
		int bytesReadExdata = 0;
		
		try {
			logger.log("Listening for NTR TCP packet...", LogLevel.VERBOSE);
			
			try {
				bytesReadHeader = socIn.readNBytes(header, 0, 84);
			} catch(IOException e) {
				bytesReadHeader = 84; // err towards logging too much, rather than nothing at all.
				throw e;
			}
			
			if(bytesReadHeader < 84) {
				logger.log("NTR recvPacket error: Received only "+bytesReadHeader+" of expected "+84+" bytes.");
				throw new Exception(); // TODO: More specific; add a message
			}
			
			pak = new Packet(header);
			
			if(pak.exdata.length > 0) {
				bytesReadExdata = socIn.readNBytes(pak.exdata, 0, pak.exdata.length);
				// maybe log some amount of exdata when this line throws an IOException?
				
				if(bytesReadExdata < pak.exdata.length) {
					// TODO: if this becomes a regular problem, maybe handle more elegantly.
					logger.log("NTR recvPacket error: Received only "+bytesReadExdata+" of expected "+pak.exdata.length+" bytes.");
					throw new Exception(); // TODO: More specific; add a message
				}
			}
		} catch(Exception e) {
			exception = e;
		}
		
		// This section logs the raw packet data, mainly.

		byte[] debugOutRawPacketData;
		if(bytesReadHeader == 84) { // full header and some exdata
			debugOutRawPacketData = new byte[84+bytesReadExdata];
			System.arraycopy(header, 0, debugOutRawPacketData, 0, 84);
			if(pak != null && pak.exdata.length > 0) {
				System.arraycopy(pak.exdata, 0, debugOutRawPacketData, 84, bytesReadExdata);
			}
		} else { // incomplete header (only)
			debugOutRawPacketData = new byte[bytesReadHeader];
			System.arraycopy(header, 0, debugOutRawPacketData, 0, bytesReadHeader);
		}
		
		if(exception != null) {
			if(debugOutRawPacketData.length > 0) {
				logger.log("NTR TCP packet received! (incomplete)");
				logger.log(debugOutRawPacketData, LogLevel.REGULAR);
			}
			throw exception;
		}
		logger.log("NTR TCP packet received!", LogLevel.EXTREME);
		logger.log(debugOutRawPacketData, LogLevel.EXTREME);
		return pak;
	}
	
	/**
	 * Converts 4 bytes of an array to a 32-bit integer. Big-endian.
	 * 
	 * @param dat	byte array
	 * @param i		starting index / offset in the byte array
	 * @return		32-bit integer
	 */
	public static int bytesToInt(byte[] dat, int i) {
		try {
			return (dat[i+3]&0xff)<<24 | (dat[i+2]&0xff)<<16 | (dat[i+1]&0xff)<<8 | (dat[i]&0xff);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace(); // TODO: change this
		}
		return 0;
	}
	
	/**
	 * Converts 4 bytes of an array to a 32-bit integer. Big-endian.
	 * 
	 * @param dat	byte array
	 * @return		32-bit integer
	 */
	public static int bytesToInt(byte[] dat) {
		return bytesToInt(dat, 0);
	}
	
	/**
	 * Convert a 32-bit integer to an array of 4 bytes. Big-endian.
	 * 
	 * @param num	integer to convert
	 * @return		array of 4 bytes
	 */
	public static byte[] intToBytes(int num) {
		byte[] data = new byte[4];
		data[0] = (byte)(num & 0xff);
		data[1] = (byte)(num>>>8 & 0xff);
		data[2] = (byte)(num>>>16 & 0xff);
		data[3] = (byte)(num>>>24 & 0xff);
		return data;
	}
	
	/** 
	 * Represents a type of NFC patch
	 */
	public static enum NFCPatchType {
		/** NFC Patch for System Update 11.3.x or lower */
		OLD,
		/** NFC Patch for System Update 11.4.x or higher */
		NEW
	}
	
	/**
	 * Represents a (TCP) packet received from NTR / NTR-HR
	 */
	private static class Packet {
		
		/** Header */
		
		/** NTR magic number. */
		// public static final int magic = 0x12345678;
		
		/** Sequence ID. More or less optional. */
		public int seq = 0;
		
		/**
		 * "Type"
		 * <p>
		 *  As a general rule, this should be 1 if the exdata section contains data,
		 *  and 0 if the exdata section is empty (0 bytes in length).
		 *  Default value is -1, which tells {@link #getRaw()} to ignore this variable
		 *  and use either 1 or 0 according to this rule.
		 * </p>
		 * 
		 * <p>In practice, it is unknown whether this variable actually affects NTR's behavior.
		 *  Refer to docs. (TODO)</p>
		 */
		public int type = -1;
		
		/**
		 * Command
		 * <p>
		 *  Required.
		 *  Default value is -1, which is an invalid command, so NTR just ignores the packet after receiving it.
		 *  For a list of valid commands, refer to docs. (TODO)
		 * </p>
		 */
		public int cmd = -1;
		
		/**
		 * Arguments. Context-dependent, based on the Command.
		 * These are unsigned 32-bit integers, but that usually doesn't matter.
		 * In this implementation, this array may be of arbitrary length between 0 and 16 (inclusive).
		 */
		public int[] args = new int[16];
		
		/** Length of the exdata section. */
		//public int exdataLen;
		
		/** Non-Header */
		
		/**
		 * Exdata section
		 * <p>
		 *  NTR calls this the "Data" section. However for the sake of clarity,
		 *  Chokistream's code and documentation will almost always refer to this as the
		 *  "Exdata" or "exdata" section. (short for "extra data")
		 * </p>
		 * 
		 * <p>This array may be of arbitrary length.</p>
		 */
		public byte[] exdata = new byte[0];
		// TODO: Implement a length limit?
		
		
		Packet(){};
		
		Packet(int seq, int type, int cmd, int[] args, byte[] exdata) {
			this.seq = seq;
			this.type = type;
			this.cmd = cmd;
			this.args = args;
			this.exdata = exdata;
		}
		
		Packet(int cmd, int[] args) {
			this.cmd = cmd;
			this.args = args;
		}
		
		/**
		 * Convert raw data into a Packet.
		 * <p>
		 *  When an exception is thrown, this Packet object may or may not have properly assigned all its variables.
		 *  Doesn't do any sanity checks on seq, type, or cmd.
		 * </p>
		 * <p>
		 *  It is acceptable for the input data to only consist of the 84-byte packet header.
		 *  In such a case, the exdata variable will be a placeholder byte array, of length specified in the header.
		 *  The intended use-case of this behavior is as follows:
		 * </p>
		 * <ol>
		 *  <li>Receive the header data over the network.</li>
		 *  <li>Pass the header data into this constructor to interpret the packet header.</li>
		 *  <li>Check the length of the exdata section. (<code>exdata.length</code>)</li>
		 *  <li>Receive the exdata over the network.</li>
		 *  <li>Fill the <code>exdata</code> of this Packet object with the exdata received.</li>
		 * </ol>
		 * 
		 * <p>Note: Other edge-case behavior related to exdata length is currently undefined,
		 *  and subject to change in this implementation. (TODO)</p>
		 * 
		 * @param pak A packet, in the form of raw bytes.
		 * @throws Exception Thrown in some cases of invalid packet data. Specifically:
		 *  <ul>
		 *  <li>If the input byte array is less than 84 bytes in length.
		 *  That is the length of the header, and likewise the minimum required length for a packet to be valid.</li>
		 *  <li>If the Magic Number is incorrect. That indicates the data is most likely malformed.</li>
		 *  </ul>
		 */
		Packet(byte[] pak) throws Exception {
			// minimum valid packet length; size of header
			if(pak.length < 84) {
				logger.log("NTRClient Packet error: Invalid packet size. "+pak.length+" bytes is too small.");
				throw new Exception(); // TODO: More specific; add a message
			}
			
			// verify magic number
			if(pak[0] != 0x78 || pak[1] != 0x56 || pak[2] != 0x34 || pak[3] != 0x12) {
				logger.log("NTRClient Packet error: Processed packet is most likely malformed.");
				throw new Exception(); // TODO: More specific; add a message
			}
			
			seq = bytesToInt(pak, 4);
			type = bytesToInt(pak, 8);
			cmd = bytesToInt(pak, 12);
			
			for(int i = 0; i < 16; i++) {
				args[i] = bytesToInt(pak, i*4+16);
			}
			
			// TODO: maybe make sure this number is (more) sane
			// Unsigned 32-bit integer
			int exdataLen = bytesToInt(pak, 80);
			
			if(exdataLen < 0) { // :(
				// unsigned int -> signed int conversion error; please don't send >2GB of data :(
				logger.log("NTRClient Packet error: Reported exdata length is "+Integer.toUnsignedString(exdataLen)+" bytes. Something has gone wrong.");
			} else if(exdataLen != 0) {
				if(pak.length == 84) { // Calling method passed header only (this is supported)
					exdata = new byte[exdataLen];
				} else {
					int expectedExdataLen = pak.length-84;
					// TODO: I'm undecided on whether to correct this mismatch issue, and how. -C
					if(expectedExdataLen != exdataLen) {
						logger.log("NTRClient Packet error: Reported exdata length ("+exdataLen+") is not equal to actual exdata length ("+expectedExdataLen+").");
						if(expectedExdataLen < exdataLen) {
							exdataLen = expectedExdataLen;
						}
					}
					exdata = new byte[exdataLen];
					System.arraycopy(pak, 84, exdata, 0, exdataLen);
				}
			}
		}
		
		/**
		 * Convert this Packet into raw data.
		 * @return byte[] result of the conversion.
		 */
		byte[] getRaw() {
			int exdataLen = exdata.length;
			byte[] pak = new byte[84+exdataLen];
			
			// magic number
			pak[0] = 0x78;
			pak[1] = 0x56;
			pak[2] = 0x34;
			pak[3] = 0x12;
			
			System.arraycopy(intToBytes(seq), 0, pak, 4, 4);
			
			int myType = type;
			if(myType == -1) {
				if(exdataLen > 0) {
					myType = 1;
				} else {
					myType = 0;
				}
			}
			System.arraycopy(intToBytes(myType), 0, pak, 8, 4);
			
			System.arraycopy(intToBytes(cmd), 0, pak, 12, 4);
			
			int argsLen = args.length;
			if(argsLen > 16) { // shouldn't ever happen; code logic error.
				// TODO: throw an exception?
				logger.log("NTRClient Packet error: args.length > 16");
				argsLen = 16;
			}
			for(int i = 0; i < argsLen; i++) {
				System.arraycopy(intToBytes(args[i]), 0, pak, i*4+16, 4);
			}
			
			System.arraycopy(intToBytes(exdataLen), 0, pak, 80, 4);
			
			if(exdataLen > 0) {
				System.arraycopy(exdata, 0, pak, 84, exdataLen);
			}
			
			return pak;
		}
	}
}
