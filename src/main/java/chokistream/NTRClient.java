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
	public static void sendNFCPatch(int chooseAddr) throws IOException {
		sendNFCPatch(host, port, chooseAddr);
	}
	
	// overloaded function; manually specify host and port.
	public static void sendNFCPatch(String host, int port, int chooseAddr) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.seq = 24000; // 0x5DC0
		pakobj.type = 1;
		pakobj.command = 10; // 0x0a
		
		pakobj.args = new int[3];
		
		pakobj.args[0] = 26; // pid; 0x1A
		pakobj.args[1] = switch(chooseAddr) { // addr
			case 0:
				yield 0x00105AE4; // Sys ver. < 11.4
			default:
				yield 0x00105B00; // Sys ver. >= 11.4
		};
		
		pakobj.exdata = new byte[] {0x70,0x47};
		
		pakobj.args[2] = pakobj.exdata.length;
		
		sendPacket(host, port, pakobj);
		logger.log("NFC Patch sent!");
	}
	
	public static void sendInitPacket(DSScreen screen, int priority, int quality, int qos) throws IOException {
		sendInitPacket(host, port, screen, priority, quality, qos);
	}
	
	public static void sendInitPacket(String host, int port, DSScreen screen, int priority, int quality, int qos) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.seq = 3000; // 0x0BB8
		pakobj.type = 0;
		pakobj.command = 901; //0x0385
		
		pakobj.args = new int[3];
		
		pakobj.args[0] = ((screen == DSScreen.TOP)? 1 : 0) << 8 | (priority % 256);
		pakobj.args[1] = quality;
		pakobj.args[2] = qos*2; // Nobody has any clue why, but NTR expects double the QoS value
		
		logger.log("Sending init packet", LogLevel.VERBOSE);
		sendPacket(host, port, pakobj);
	}
	
	public static void sendHeartbeatPacket() throws IOException {
		sendHeartbeatPacket(host, port);
	}
	
	public static void sendHeartbeatPacket(String host, int port) throws IOException {
		//if(heartbeatSendable == 1) {
		NTRPacket pakobj = new NTRPacket();
		sendPacket(host, port, pakobj);
		//heartbeatSendable = 0;
	}
	
	public static void sendHelloPacket() throws IOException {
		sendHelloPacket(host, port);
	}
	
	public static void sendHelloPacket(String host, int port) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.command = 3;
		sendPacket(host, port, pakobj);
	}
	
	public static void sendReloadPacket() throws IOException {
		sendReloadPacket(host, port);
	}
	
	public static void sendReloadPacket(String host, int port) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.command = 4;
		sendPacket(host, port, pakobj);
	}
	
	public static void sendReadMemPacket(int addr, int size, int pid, String fileName) throws IOException {
		sendReadMemPacket(host, port, addr, size, pid, fileName);
	}
	
	public static void sendReadMemPacket(String host, int port, int addr, int size, int pid, String fileName) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.command = 9;
		pakobj.args = new int[] {pid, addr, size};
		
		//lastReadMemSeq = currentSeq;
		//pakobj.seq = lastReadMemSeq;
		//lastReadMemFileName = fileName;
		
		sendPacket(host, port, pakobj);
	}
	
	public static void sendWriteMemPacket(int addr, int pid, byte[] buf) throws IOException {
		sendWriteMemPacket(host, port, addr, pid, buf);
	}
	
	public static void sendWriteMemPacket(String host, int port, int addr, int pid, byte[] buf) throws IOException {
		NTRPacket pakobj = new NTRPacket();
		pakobj.type = 1;
		pakobj.command = 10;
		pakobj.args = new int[] {pid, addr, buf.length};
		pakobj.exdata = buf;
		
		sendPacket(host, port, pakobj);
	}
	
	public static void sendSaveFilePacket(String fileName, byte[] fileData) throws IOException {
		sendSaveFilePacket(host, port, fileName, fileData);
	}
	
	public static void sendSaveFilePacket(String host, int port, String fileName, byte[] fileData) throws IOException {
		byte[] fileNameBuf = fileName.getBytes(StandardCharsets.UTF_8);
		byte[] combinedFileBuf = new byte[fileNameBuf.length + fileData.length];
		copyByteArray(fileNameBuf, combinedFileBuf, 0);
		copyByteArray(fileData, combinedFileBuf, fileNameBuf.length);
		
		NTRPacket pakobj = new NTRPacket();
		pakobj.type = 1;
		pakobj.command = 1;
		pakobj.exdata = combinedFileBuf;
		
		sendPacket(host, port, pakobj);
	}
	
	public static void sendPacket(NTRPacket pakobj) throws IOException {
		sendPacket(host, port, pakobj);
	}
	
	public static void sendPacket(String host, int port, NTRPacket pakobj) throws IOException {
		int dataLen = pakobj.exdata.length;
		
		byte[] pak = new byte[84+dataLen];
		
		copyByteArray(intToBytes(0x12345678), pak, 0);
		
		copyByteArray(intToBytes(pakobj.seq), pak, 4);
		copyByteArray(intToBytes(pakobj.type), pak, 8);
		copyByteArray(intToBytes(pakobj.command), pak, 12);
		
		// arguments
		int argmax = pakobj.args.length;
		if(argmax > 16)
			argmax = 16;
		for(int i = 0; i < argmax; i++) {
			copyByteArray(intToBytes(pakobj.args[i]), pak, i*4+16);
		}
		
		copyByteArray(intToBytes(dataLen), pak, 80);
		copyByteArray(pakobj.exdata, pak, 84);
		
		logger.log("Sending packet to NTR...", LogLevel.EXTREME);
		logger.log(pak, LogLevel.EXTREME);
		
		Socket mySoc = new Socket(host, port);
		//mySoc.setTcpNoDelay(true);
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
	private static int bytesToInt(byte[] dat, int i) {
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
	private static int bytesToInt(byte[] dat) {
		return bytesToInt(dat, 0);
	}
	
	/**
	 * Convert a 32-bit integer to an array of 4 bytes. Big-endian.
	 * 
	 * @param num	integer to convert
	 * @return		array of 4 bytes
	 */
	private static byte[] intToBytes(int num) {
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
	private static void copyByteArray(byte[] src, byte[] dst, int i) {
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
	 * Represents a packet received from or sent to NTR
	 */
	private static class NTRPacket {
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
			exdata = new byte[0];
		}
		
		public NTRPacket(int myseq, int mytype, int mycmd, int[] myarg, byte[] myexdat) {
			seq = myseq;
			type = mytype;
			command = mycmd;
			args = myarg;
			exdata = myexdat;
		}
	}
}
