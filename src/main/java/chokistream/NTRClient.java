/**
 * NTRClient.java
 * Copyright (C) 2023  Eiim, ChainSwordCS, Herronjo
 * Some code is based on NTRClient, which is Copyright (C) 2016  Cell9 / 44670
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Thread used by NTRClient to read and buffer Frames received from the 3DS.
	 */
	private NTRUDPThread thread;
	
	private static final Logger logger = Logger.INSTANCE;
	
	public static String host;
	public static int port;
	
	private static int currentSeq;
	
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
	public NTRClient(String newHost, int quality, DSScreen screen, int priority, int qos, ColorMode colorMode, int newPort) throws UnknownHostException, IOException, InterruptedException {
		host = newHost;
		port = newPort;
		currentSeq = 0;
		sendInitPacket(screen, priority, quality, qos);
		
		thread = new NTRUDPThread(screen, colorMode);
		thread.start();

		// Give NTR some time to think
		TimeUnit.SECONDS.sleep(3);
		
		// NTR expects us to reconnect, so we will. And then disconnect again!
		Socket client = new Socket(host, 8000);
		client.close();
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
	
	// overloaded function; use stored host and port.
	public static void sendNFCPatch(int chooseAddr) {
		sendNFCPatch(host, port, chooseAddr);
	}
	
	// overloaded function; manually specify host and port.
	public static void sendNFCPatch(String host, int port, int chooseAddr) {
		NTRPacket packet = new NTRPacket();
		packet.seq = 24000;
		int seq = 24000; // 0x5DC0
		int type = 1;
		int cmd = 10; // 0x0a
		
		int[] args = new int[3];
		
		args[0] = 26; // pid; 0x1A
		args[1] = switch(chooseAddr) { // addr
			case 0:
				yield 0x00105AE4; // Sys ver. < 11.4
			default:
				yield 0x00105B00; // Sys ver. >= 11.4
		};
		
		byte[] exdata = {0x70,0x47};
		
		args[2] = exdata.length;
		
		try {
			sendPacket(host, port, type, cmd, args, exdata, seq);
			logger.log("NFC Patch sent!");
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("NFC Patch failed to send");
		}
	}
	
	public static void sendInitPacket(DSScreen screen, int priority, int quality, int qos) {
		sendInitPacket(host, port, screen, priority, quality, qos);
	}
	
	public static void sendInitPacket(String host, int port, DSScreen screen, int priority, int quality, int qos) {
		int seq = 3000; // 0x0BB8
		int type = 0;
		int cmd = 901; //0x0385
		
		int[] args = new int[16];
		
		args[0] = ((screen == DSScreen.TOP)? 1 : 0) << 8 | (priority % 256);
		args[1] = quality;
		args[2] = qos*2; // Nobody has any clue why, but NTR expects double the QoS value
		
		try {
		logger.log("Sending init packet", LogLevel.VERBOSE);
		sendPacket(host, port, type, cmd, args, new byte[0], seq);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("Init packet failed to send");
		}
		
		return;
	}
	
	public static void sendHeartbeatPacket() {
		sendHeartbeatPacket(host, port);
	}
	
	public static void sendHeartbeatPacket(String host, int port) {
		//if(heartbeatSendable == 1) {
		try {
			sendPacket(host, port, 0, 0, new int[0], new byte[0]);
			//heartbeatSendable = 0;
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("Heartbeat packet failed to send");
		}
		return;
	}
	
	public static void sendHelloPacket() {
		sendHelloPacket(host, port);
	}
	
	public static void sendHelloPacket(String host, int port) {
		try {
			sendPacket(host, port, 0, 3, new int[0], new byte[0]);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("Hello packet failed to send");
		}
	}
	
	public static void sendReloadPacket() {
		sendReloadPacket(host, port);
	}
	
	public static void sendReloadPacket(String host, int port) {
		try {
			sendPacket(host, port, 0, 4, new int[0], new byte[0]);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("Hello packet failed to send");
		}
	}
	
	public static void sendReadMemPacket(int addr, int size, int pid, String fileName) {
		sendReadMemPacket(host, port, addr, size, pid, fileName);
	}
	
	public static void sendReadMemPacket(String host, int port, int addr, int size, int pid, String fileName) {
		int args[] = new int[3];
		args[0] = pid;
		args[1] = addr;
		args[2] = size;
		
		//lastReadMemSeq = currentSeq;
		//lastReadMemFileName = fileName;
		
		try {
			sendPacket(host, port, 0, 9, args, new byte[0]);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("ReadMem packet failed to send");
		}
		return;
	}
	
	public static void sendWriteMemPacket(int addr, int pid, byte[] buf) {
		sendWriteMemPacket(host, port, addr, pid, buf);
	}
	
	public static void sendWriteMemPacket(String host, int port, int addr, int pid, byte[] buf) {
		int args[] = new int[16];
		args[0] = pid;
		args[1] = addr;
		args[2] = buf.length;
		
		try {
			sendPacket(host, port, 1, 10, args, buf);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("WriteMem packet failed to send");
		}
		return;
	}
	
	public static void sendSaveFilePacket(String fileName, byte[] fileData) {
		sendSaveFilePacket(host, port, fileName, fileData);
	}
	
	public static void sendSaveFilePacket(String host, int port, String fileName, byte[] fileData) {
		byte[] fileNameBuf = fileName.getBytes(StandardCharsets.UTF_8);
		byte[] combinedFileBuf = new byte[fileNameBuf.length + fileData.length];
		copyByteArray(fileNameBuf, combinedFileBuf, 0);
		copyByteArray(fileData, combinedFileBuf, fileNameBuf.length);
		
		try {
			sendPacket(host, port, 1, 1, new int[0], combinedFileBuf);
		} catch(IOException e) {
			e.printStackTrace();
			logger.log("SaveFile packet failed to send");
		}
		return;
	}
	
	public static void sendPacket(String host, int port, int type, int cmd, int[] args, byte[] exdata) throws UnknownHostException, IOException {
		try {
			sendPacket(host, port, type, cmd, args, exdata, 0);
		} catch(IOException e) {
			throw e;
		}
		return;
	}
	
	public static void sendPacket(String host, int port, int type, int cmd, int[] args, byte[] exdata, int seq) throws UnknownHostException, IOException {
		int dataLen = exdata.length;
		
		byte[] pak = new byte[84+dataLen];
		
		copyByteArray(intToBytes(0x12345678), pak, 0);
		
		copyByteArray(intToBytes(seq), pak, 4);
		copyByteArray(intToBytes(type), pak, 8);
		copyByteArray(intToBytes(cmd), pak, 12);
		
		// arguments
		int argmax = args.length;
		if(argmax > 16)
			argmax = 16;
		for(int i = 0; i < argmax; i++) {
			copyByteArray(intToBytes(args[i]), pak, i*4+16);
		}
		
		copyByteArray(intToBytes(dataLen), pak, 80);
		copyByteArray(exdata, pak, 84);
		
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		
		Socket mySoc = new Socket(host, port);
		//mySoc.setTcpNoDelay(true);
		OutputStream myOut = mySoc.getOutputStream();
		myOut.write(pak);
		myOut.close();
		mySoc.close();
		return;
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
			e.printStackTrace();
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
	 * Copies all values from source byte array to destination byte array, starting at a given index in the destination byte array.
	 * 
	 * @param src	source byte array
	 * @param dst	destination byte array
	 * @param i		starting index / offset of the destination byte array.
	 */
	public static void copyByteArray(byte[] src, byte[] dst, int i) {
		try {
			for(int j = 0; j < src.length ; j++) {
				dst[i+j] = src[j];
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * currently unused
	 * Represents a packet received from or sent to NTR
	 */
	public static class NTRPacket {
		public int seq;
		public int type;
		public int command;
		public int[] args;
		public byte[] exdata;
		
		public NTRPacket() {
			seq = currentSeq;
			type = 0;
			command = 0;
			args = new int[16];
		}
		
		public void resetSeq() {
			seq = currentSeq;
		}
		
		public void setDefaults(int type) {
			
			switch(type) {
			
			case 0:
				break;
			default:
				break;
			
			}
			
			return;
		}
		
	}
}
