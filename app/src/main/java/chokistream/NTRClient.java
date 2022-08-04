package chokistream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class NTRClient implements StreamingInterface {
	
	/**
	 * Thread used by NTRClient to read and buffer Frames received from the 3DS.
	 */
	private NTRUDPThread thread;

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
	public NTRClient(String host, int quality, NTRScreen screen, int priority, int qos, ColorMode colorMode, int port) throws UnknownHostException, IOException, InterruptedException {
		// Connect to TCP port and set up client
		Socket client = new Socket(host, port);
		client.setTcpNoDelay(true);
		OutputStream out = client.getOutputStream();
		thread = new NTRUDPThread(host, screen, colorMode);
		thread.start();
		
		// Creates and sends the initialization packet to the 3DS
		byte[] initializationPacket = new byte[84];
		initializationPacket[0] = 0x78;
		initializationPacket[1] = 0x56;
		initializationPacket[2] = 0x34;
		initializationPacket[3] = 0x12;
		initializationPacket[4] = (byte) 0xb8;
		initializationPacket[5] = 0x0b;
		initializationPacket[12] = (byte) 0x85;
		initializationPacket[13] = 0x03;
		initializationPacket[16] = (byte) priority;
		initializationPacket[17] = (byte) ((screen == NTRScreen.TOP) ? 0x01 : 0x00);
		initializationPacket[20] = (byte) quality;
		// Nobody has any clue why, but NTR expects double the QoS value
		initializationPacket[26] = (byte) (qos*2);
		
		out.write(initializationPacket);
		
		// NTR expects us to disconnect
		client.close();
		
		// Give NTR some time to think
		TimeUnit.SECONDS.sleep(3);
		
		// NTR expects us to reconnect, so we will. And then disconnect again!
		client = new Socket(host, 8000);
		client.close();
	}

	@Override
	public void close() throws IOException {
		thread.interrupt();
		thread.close();
	}

	@Override
	public Frame getFrame() throws InterruptedException {
		return thread.getFrame();
	}
	
	public static void sendNFCPatch(String host, int port, byte[] addr, ConsoleModel model) throws UnknownHostException, IOException {
		byte[] binaryPacketPatch = new byte[11 + addr.length];
		binaryPacketPatch[0] = (byte) 0x81;
		binaryPacketPatch[1] = 0x0A;
		binaryPacketPatch[4] = (byte) ((model == ConsoleModel.N3DS) ? 0x1A : 0x19);
		binaryPacketPatch[binaryPacketPatch.length - 2] = 0x70;
		binaryPacketPatch[binaryPacketPatch.length - 1] = 0x47;
		
		Socket patchClient = new Socket(host, port);
		OutputStream patchOut = patchClient.getOutputStream();
		patchOut.write(binaryPacketPatch);
		patchOut.close();
		patchClient.close();
	}
}
