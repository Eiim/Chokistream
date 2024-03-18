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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Thread used by NTRClient to read and buffer Frames received from the 3DS.
	 */
	private final NTRUDPThread thread;
	
	private final Random random = new Random();
	
	private static final Logger logger = Logger.INSTANCE;
	
	private int topFrames;
	private int bottomFrames;
	
	private Socket soc = null;
	private OutputStream socOut = null;
	private InputStream socIn = null;

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
		thread = new NTRUDPThread(screen, colorMode, port);
		thread.start();
		
		try {
			//reopenSocket(host);
			soc = new Socket(host, 8000);
			soc.setSoTimeout(10000);
			socOut = soc.getOutputStream();
			socIn = soc.getInputStream();
			
			sendInitPacket(port, screen, priority, quality, qos);
			
			// Give NTR some time to think
			TimeUnit.SECONDS.sleep(3);
			
			String heartbeatReply = heartbeat();
			
			// NTR (3.6 or 3.6.1) needs to reload to reinitialize quality, priority screen, etc. (?)
			
			// This is a somewhat hacky solution because a proper one doesn't exist.
			// TODO: Account for the possible presence of irrelevant backlog debug output? (This *should* be harmless though.)
			if(heartbeatReply.contains("remote play already started")) {
				//logger.log("Reloading NTR...");
				//sendReloadPacket();
				//TimeUnit.SECONDS.sleep(3);
				//reopenSocket(host);
				//sendInitPacket(port, screen, priority, quality, qos);
				//TimeUnit.SECONDS.sleep(3);
				//heartbeat();
			}
		
		} catch (ConnectException e) {
			if(thread.isReceivingFrames()) {
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

	@Override
	public void close() throws IOException {
		thread.interrupt();
		thread.close();
		if(soc != null && !soc.isClosed()) {
			soc.close();
		}
	}

	@Override
	public Frame getFrame() throws InterruptedException {
		Frame f = thread.getFrame();
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
	 * (Re-) Opens the Socket at the specified IP address.
	 * 
	 * @param host Host (IP address) of the 3DS.
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void reopenSocket(String host) throws SocketException, UnknownHostException, IOException {
		if(soc != null && !soc.isClosed()) {
			soc.close();
		} else {
			logger.log("NTR reopenSocket warning: Socket is null or already closed.");
		}
		
		try {
			Socket newSoc = new Socket(host, 8000);
			soc = newSoc;
			soc.setSoTimeout(10000);
			socOut = soc.getOutputStream();
			socIn = soc.getInputStream();
		} catch (Exception e) {
			// TODO: Maybe close soc, for the sake of predictable behavior.
			throw e;
		}
	}
	
	public String heartbeat() throws Exception, IOException {
		Packet pak = new Packet();
		int heartbeatSeq = random.nextInt(100);
		pak.seq = heartbeatSeq;
		pak.type = 0;
		pak.cmd = 0; // heartbeat command
		
		try {
			logger.log("Sending NTR Heartbeat packet...");
			sendPacket(pak);
			logger.log("NTR Heartbeat packet sent.");
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
		
		// TODO: sooner or later i'll implement this very differently. but this should work fine for now. -C
		if(reply.cmd != 0 || reply.seq != heartbeatSeq) {
			logger.log("NTR Heartbeat error: Received non-matching response packet.");
			if(reply.cmd != 0) {
				logger.log("cmd "+reply.cmd+" != 0");
			}
			if(reply.seq != heartbeatSeq) {
				logger.log("seq "+reply.seq+" != "+heartbeatSeq);
			}
			throw new Exception();
		}
		
		logger.log("NTR Heartbeat response received.");
		
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
			logger.log("NTR Heartbeat response is empty.");
			return new String("");
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
	
	/**
	 * dummied out
	 */
	public static void sendNFCPatch(String host, int chooseAddr) {
		
	}
	
	public void sendNFCPatch(int chooseAddr) {
		Packet pak = new Packet();
		pak.seq = 24000;
		pak.type = 1;
		pak.cmd = 10;
		
		pak.args[0] = 26; // pid; 0x1A
		pak.args[1] = switch(chooseAddr) {
			case 0:
				yield 0x00105AE4; // Sys ver. < 11.4
			default:
				yield 0x00105B00; // Sys ver. >= 11.4
		};
		
		pak.exdata = new byte[] {0x70,0x47};
		pak.args[2] = pak.exdata.length;
		
		try {
			sendPacket(pak);
			logger.log("NFC Patch sent!");
		} catch(IOException e) {
			e.printStackTrace(); // TODO: change this?
			logger.log("NFC Patch failed to send");
		}
	}
	
	public void sendInitPacket(int port, DSScreen screen, int priority, int quality, int qos) throws IOException {
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
	
	public void sendPacket(Packet packet) throws IOException {
		byte[] pak = packet.getRaw();
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		socOut.write(pak);
	}
	
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
				throw new Exception();
			}
			
			pak = new Packet(header);
			
			if(pak.exdata.length > 0) {
				bytesReadExdata = socIn.readNBytes(pak.exdata, 0, pak.exdata.length);
				// maybe log some amount of exdata when this line throws an IOException?
				
				if(bytesReadExdata < pak.exdata.length) {
					// TODO: if this becomes a regular problem, maybe handle more elegantly.
					logger.log("NTR recvPacket error: Received only "+bytesReadExdata+" of expected "+pak.exdata.length+" bytes.");
					throw new Exception();
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
	 * Represents a (TCP) packet received from NTR / NTR-HR
	 */
	private static class Packet {
		
		/* Header */
		
		/* NTR magic number. */
		public final int magic = 0x12345678;
		
		/* Sequence ID. More or less optional. */
		public int seq = 0;
		
		/* "Type."
		 * Traditionally set to 0 if the Extra Data section is empty, and 1 otherwise.
		 * This may or may not matter to NTR. Refer to docs. (TODO)
		 */
		public int type = -1; // placeholder;
		
		/* Command. Required. */
		public int cmd = -1;
		
		/**
		 * Arguments. Context-dependent, based on the Command.
		 * Supports arbitrary array length between 0 and 16 (inclusive).
		 * Technically unsigned 32-bit integers.
		 */
		public int[] args = new int[16];
		
		/* Length of the Extra Data section (exdata). */
		//public int exdataLen;
		
		/* Non-Header */
		
		/**
		 *  Extra Data (aka "Data") section.
		 *  Supports arbitrary array length.
		 */
		// TODO: Implement a length limit?
		public byte[] exdata = new byte[0];
		
		
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
		 * 
		 * The input data doesn't necessarily have to contain the corresponding exdata; the header alone is enough.
		 * In such a case where the exdata is not present, a placeholder byte array will be created for exdata, of the length specified in the header.
		 * 
		 * @param pak A packet, in the form of raw bytes.
		 */
		Packet(byte[] pak) throws Exception {
			// minimum valid packet length; size of header
			if(pak.length < 84) {
				logger.log("NTRClient Packet error: Invalid packet size. "+pak.length+" bytes is too small.");
				throw new Exception();
			}
			
			// verify magic number
			if(pak[0] != 0x78 || pak[1] != 0x56 || pak[2] != 0x34 || pak[3] != 0x12) {
				logger.log("NTRClient Packet error: Processed packet is most likely malformed.");
				throw new Exception();
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
