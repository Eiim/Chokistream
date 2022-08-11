package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import chokistream.props.ColorMode;
import javafx.scene.image.Image;

/**
 * 
 */
public class HZModClient implements StreamingInterface {
	
	private final byte targaPacket = 0x03;
	private final byte jpegPacket = 0x04;
	
	private Socket client = null;
	private InputStream in = null;
	private OutputStream out = null;
	private ColorMode colorMode;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 */
	public HZModClient(String host, int quality, int capCPU, ColorMode receivedColorMode, int port) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, 6464);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
		colorMode = receivedColorMode;
		
		if (capCPU > 0) {
			// Creates the limit CPU packet to the 3DS
			byte[] limitCPUPacket = new byte[9];
			limitCPUPacket[0] = 0x7E;
			limitCPUPacket[1] = 0x05;
			limitCPUPacket[4] = (byte) 0xFF;
			limitCPUPacket[8] = (byte) capCPU;
			out.write(limitCPUPacket);
		}
		
		// Creates the quality packet to the 3DS
		byte[] qualityPacket = new byte[9];
		qualityPacket[0] = 0x7E;
		qualityPacket[1] = 0x05;
		qualityPacket[4] = 0x03;
		qualityPacket[8] = (byte) quality;
		out.write(qualityPacket);
		
		// Creates the initialization packet to the 3DS
		byte[] initializationPacket = new byte[9];
		initializationPacket[0] = 0x7E;
		initializationPacket[1] = 0x05;
		initializationPacket[8] = 0x01;
		
		out.write(initializationPacket);
		
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.close();
		client.close();
	}

	/**
	 * Get a packet from HzMod
	 * @return The packet received from HzMod
	 * @throws IOException 
	 */
	private Packet getPacket() throws IOException {
		Packet returnPacket = new Packet();
		
		returnPacket.type = (byte) in.read();
		returnPacket.length = in.read() + (in.read() << 8) + (in.read() << 16);
		returnPacket.data = in.readNBytes(returnPacket.length);
		
		return returnPacket;
	}
	
	@Override
	public Frame getFrame() throws IOException {
		Frame returnFrame = null;
		Packet packet = new Packet();
		
		while (packet.type != jpegPacket && packet.type != targaPacket) {
			packet = getPacket();
		}
		
		/*
		 * No clue why, but HzMod includes an extra 8 bytes at the front of the image.
		 * We need to trim it off.
		 */
		byte[] data = Arrays.copyOfRange(packet.data, 8, packet.data.length);
		
		Image image = null;
		
		if (packet.type == jpegPacket) {
			WritableInputStream imageData = new WritableInputStream(data, true);
			image = new Image(imageData.getInputStream());
		} else if (packet.type == targaPacket) {
			// TODO implement TARGA support
		}
		
		/*
		 * For some reason the red and blue channels are swapped.
		 * Fix it.
		 */
		image = ColorHotfix.doColorHotfix(image, colorMode, true);
		
		returnFrame = new Frame(image);
		
		return returnFrame;
	}
	
	/**
	 * Represents a packet received from HzMod
	 */
	private class Packet {
		public byte type;
		public int length;
		public byte[] data;
	}
	
}
