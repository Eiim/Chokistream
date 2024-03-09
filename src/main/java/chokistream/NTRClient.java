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
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Thread used by NTRClient to read and buffer Frames received from the 3DS.
	 */
	private final NTRUDPThread thread;
	
	private static final Logger logger = Logger.INSTANCE;
	
	private int topFrames;
	private int bottomFrames;
	
	private Socket soc;
	private OutputStream socOut;
	private InputStream socIn;

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
	public NTRClient(String host, int quality, DSScreen screen, int priority, int qos, ColorMode colorMode, int port) throws UnknownHostException, IOException, InterruptedException {
		thread = new NTRUDPThread(screen, colorMode, port);
		thread.start();
		
		soc = new Socket(host, 8000);
		socOut = soc.getOutputStream();
		socIn = soc.getInputStream();
		
		try {
			
			sendInitPacket(port, screen, priority, quality, qos);
			
			// Give NTR some time to think
			TimeUnit.SECONDS.sleep(3);
			
			heartbeat();
			TimeUnit.SECONDS.sleep(5);
			heartbeat();
		
		} catch (ConnectException e) {
			if(thread.isReceivingFrames()) {
				logger.log(e.getClass()+": "+e.getMessage()+System.lineSeparator()+Arrays.toString(e.getStackTrace()), LogLevel.VERBOSE);
				logger.log("NTR's NFC Patch seems to be active. Proceeding as normal...");
			} else {
				throw e;
			}
		}
	}

	@Override
	public void close() throws IOException {
		thread.interrupt();
		thread.close();
		socOut.close();
		socIn.close();
		soc.close();
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
	
	public void heartbeat() throws UnknownHostException, ConnectException, IOException {
		Packet pak = new Packet();
		pak.seq = 0;
		pak.type = 0;
		pak.cmd = 0; // heartbeat command
		
		try {
			sendPacket(pak);
			logger.log("heartbeat packet sent");
		} catch(IOException e) {
			logger.log("heartbeat packet failed to send");
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
		logger.log("heartbeat response received");
		
		if(reply.exdata.length > 0) {
			String debugOut = new String(reply.exdata, StandardCharsets.UTF_8);
			// (dumb) custom formatting
			debugOut = debugOut.replace("\n", "\n[NTR] ");
			logger.log("[NTR] "+debugOut, LogLevel.REGULAR);
		} else {
			logger.log("heartbeat response exdata is empty...");
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
	
	public void sendInitPacket(int port, DSScreen screen, int priority, int quality, int qos) throws UnknownHostException, ConnectException, IOException {
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
	
	public void sendPacket(Packet packet) throws UnknownHostException, ConnectException, IOException {
		byte[] pak = packet.getRaw();
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		socOut.write(pak);
	}
	
	// I apologize for the unnecessarily verbose error logging logic. That's what most of this code is. -C
	public Packet recvPacket() throws UnknownHostException, ConnectException, IOException {
		logger.log("Listening for TCP packet from NTR...", LogLevel.VERBOSE);
		byte[] header = new byte[84];
		
		socIn.read(header); // less safe version of this commented-out section
		/*
		int result;
		result = socIn.readNBytes(header, 0, 84);
		if(result != 84) {
			logger.log("NTRClient recvPacket error: received only "+result+" of expected "+84+" bytes. Aborting...");
			if(result > 0) {
				byte[] errorPacketOutput = new byte[result];
				System.arraycopy(header, 0, errorPacketOutput, 0, result);
				logger.log(errorPacketOutput, LogLevel.EXTREME);
			}
			// TODO: panic
			throw new IOException();
		}
		*/
		
		Packet pak;
		try {
			pak = new Packet(header);
		} catch(Exception e) {
			// TODO: Packet constructor throwing exceptions isn't implemented yet btw
			throw e;
		}
		
		byte[] exdata = new byte[0];
		if(pak.exdata.length != 0) {
			exdata = new byte[pak.exdata.length];
			
			socIn.read(exdata); // less safe version of this commented-out section
			/*
			result = socIn.readNBytes(exdata, 0, exdata.length);
			
			if(result != exdata.length) {
				logger.log("NTRClient recvPacket error: received only "+result+" of expected "+exdata.length+" bytes. Aborting...");
				if(result > 0) {
					byte[] errorPacketOutput = new byte[84+result];
					System.arraycopy(header, 0, errorPacketOutput, 0, 84);
					System.arraycopy(exdata, 0, errorPacketOutput, 84, result);
					logger.log(errorPacketOutput, LogLevel.EXTREME);
				} else {
					logger.log(header, LogLevel.EXTREME);
				}
				// TODO: panic
				throw new IOException();
			}
			*/
			pak.exdata = exdata;
		}
		
		byte[] logPacketOutput;
		if(exdata.length > 0) {
			logPacketOutput = new byte[84+exdata.length];
			System.arraycopy(header, 0, logPacketOutput, 0, 84);
			System.arraycopy(exdata, 0, logPacketOutput, 84, exdata.length);
		} else {
			logPacketOutput = header;
		}
		logger.log("NTR TCP packet received!", LogLevel.EXTREME);
		logger.log(logPacketOutput, LogLevel.EXTREME);
		
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
		public int cmd;
		
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
		Packet(byte[] pak) {
			if(pak.length < 84) {
				// TODO: throw an exception?
				logger.log("NTRClient Packet error: pak.length < 84");
				return;
			}
			
			// verify magic number
			if(pak[0] != 0x78 || pak[1] != 0x56 || pak[2] != 0x34 || pak[3] != 0x12) {
				// TODO: throw an exception?
				logger.log("Processed NTR packet does not seem to match the expected format.");
				return;
			}
			
			seq = bytesToInt(pak, 4);
			type = bytesToInt(pak, 8);
			cmd = bytesToInt(pak, 12);
			
			for(int i = 0; i < 16; i++) {
				args[i] = bytesToInt(pak, i*4+16);
			}
			
			// Unsigned 32-bit integer
			int exdataLen = bytesToInt(pak, 80);
			
			if(exdataLen < 0) { // :(
				// unsigned int -> signed int conversion error; please don't send >2GB of data :(
				logger.log("NTRClient Packet error: exdataLen < 0. exdataLen = "+exdataLen);
			} else if(exdataLen != 0) {
				if(pak.length == 84) { // Calling method passed header only (this is supported)
					exdata = new byte[exdataLen];
				} else {
					int expectedExdataLen = pak.length-84;
					if(expectedExdataLen != exdataLen) { // shouldn't ever happen; code logic error.
						logger.log("NTRClient Packet error: pak.length - 84 != exdataLen. "+expectedExdataLen+" != "+exdataLen);
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
