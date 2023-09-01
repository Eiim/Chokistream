package chokistream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import chokistream.props.ColorMode;
import chokistream.props.ConsoleModel;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Thread used by NTRClient to read and buffer Frames received from the 3DS.
	 */
	private NTRUDPThread thread;
	
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
		// Connect to TCP port and set up client
		Socket client = new Socket(host, port);
		client.setTcpNoDelay(true);
		OutputStream out = client.getOutputStream();
		thread = new NTRUDPThread(screen, colorMode);
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
		initializationPacket[17] = (byte) ((screen == DSScreen.TOP) ? 0x01 : 0x00);
		initializationPacket[20] = (byte) quality;
		// Nobody has any clue why, but NTR expects double the QoS value
		initializationPacket[26] = (byte) (qos*2);
		logger.log("Sending initialization packet", LogLevel.EXTREME);
		logger.log(initializationPacket, LogLevel.EXTREME);
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
		Frame f = thread.getFrame();
		if(f.screen == DSScreen.TOP) {
			topFrames++;
		} else {
			bottomFrames++;
		}
		return f;
	}
	
	public static void sendNFCPatch(String host, int port, byte[] addr) throws UnknownHostException, IOException {
		byte[] pak = new byte[84+2];
		
		// magic number / secret code (dumb)
		pak[3] = 0x12;
		pak[2] = 0x34;
		pak[1] = 0x56;
		pak[0] = 0x78;
		
		// currentSeq (?)
		pak[4] = (byte) 0xc0;
		pak[5] = 0x5d;
		
		// type
		pak[8] = 0x01;
		
		// command
		pak[12] = 0x0a;
		
		// "args" section
		
		// pid
		pak[16] = 0x1a;
		
		// address
		pak[20] = addr[0];
		pak[21] = addr[1];
		pak[22] = addr[2];
		pak[23] = addr[3];
		
		// length of data section (written in two places, for some reason)
		pak[24] = 0x02;
		pak[80] = 0x02;
		
		// data section
		pak[84] = 0x70;
		pak[85] = 0x47;
		
		Socket patchClient = new Socket(host, port);
		OutputStream patchOut = patchClient.getOutputStream();
		patchOut.write(pak);
		patchOut.close();
		patchClient.close();
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
}
