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
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
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
		
		try {
			
			sendInitPacket(host, port, screen, priority, quality, qos);
			
			// Give NTR some time to think
			TimeUnit.SECONDS.sleep(3);
			
			// NTR expects us to reconnect, so we will. And then disconnect again!
			Socket client = new Socket(host, 8000);
			client.close();
		
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
	
	public static void sendNFCPatch(String host, int chooseAddr) {
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
			sendPacket(host, pak);
			logger.log("NFC Patch sent!");
		} catch(IOException e) {
			e.printStackTrace(); // TODO: change this?
			logger.log("NFC Patch failed to send");
		}
	}
	
	public static void sendInitPacket(String host, int port, DSScreen screen, int priority, int quality, int qos) throws UnknownHostException, ConnectException, IOException {
		Packet pak = new Packet();
		pak.seq = 3000;
		pak.type = 0;
		pak.cmd = 901;
		
		pak.args[0] = ((screen == DSScreen.TOP)? 1 : 0) << 8 | (priority % 256);
		pak.args[1] = quality;
		pak.args[2] = (qos*2) << 16; // Convert to the format expected by NTR and NTR-HR
		
		try {
			logger.log("Sending init packet", LogLevel.VERBOSE);
			sendPacket(host, pak);
		} catch(IOException e) {
			logger.log("Init packet failed to send");
			throw e;
		}
	}
	
	public static void sendPacket(String host, Packet packet) throws UnknownHostException, ConnectException, IOException {
		byte[] pak = packet.getRaw();
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		Socket mySoc = new Socket(host, 8000);
		OutputStream myOut = mySoc.getOutputStream();
		myOut.write(pak);
		myOut.close();
		mySoc.close();
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
			return dat[i+3]<<24 | dat[i+2]>>8 & 0xff00 | dat[i+1]<<8 & 0xff0000 | dat[i]>>>24;
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
			
			if(exdataLen > 0) {
				int expectedExdataLen = pak.length-84;
				if(expectedExdataLen != exdataLen) { // shouldn't ever happen; code logic error.
					logger.log("NTRClient Packet error: pak.length - 84 != exdataLen. "+expectedExdataLen+" != "+exdataLen);
					if(expectedExdataLen < exdataLen) {
						exdataLen = expectedExdataLen;
					}
				}
				exdata = new byte[exdataLen];
				System.arraycopy(pak, 84, exdata, 0, exdataLen);
			} else if(exdataLen < 0) { // :(
				logger.log("NTRClient Packet error: exdataLen < 0. exdataLen = "+exdataLen);
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
