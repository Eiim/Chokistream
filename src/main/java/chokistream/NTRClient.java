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
		thread = new NTRUDPThread(screen, colorMode);
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
	
	public static void sendNFCPatch(String host, int port, int chooseAddr) {
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
	
	public static void sendInitPacket(String host, int port, DSScreen screen, int priority, int quality, int qos) throws UnknownHostException, ConnectException, IOException {
		int seq = 3000; // 0x0BB8
		int type = 0;
		int cmd = 901; //0x0385
		
		int[] args = new int[16];
		
		args[0] = ((screen == DSScreen.TOP)? 1 : 0) << 8 | (priority % 256);
		args[1] = quality;
		args[2] = (qos*2) << 16; // Convert to the format expected by NTR and NTR-HR
		
		try {
			logger.log("Sending init packet", LogLevel.VERBOSE);
			sendPacket(host, port, type, cmd, args, new byte[0], seq);
		} catch(IOException e) {
			logger.log("Init packet failed to send");
			throw e;
		}
	}
	
	public static void sendPacket(String host, int port, int type, int cmd, int[] args, byte[] exdata, int seq) throws UnknownHostException, ConnectException, IOException {
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
}
