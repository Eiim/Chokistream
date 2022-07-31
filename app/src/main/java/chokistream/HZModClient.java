package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 */
	public HZModClient(String host, int quality, int capCPU) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, 6464);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
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
		in.readNBytes(8);
		returnPacket.data = in.readNBytes(returnPacket.length - 8);
		
		return returnPacket;
	}
	
	@Override
	public Frame getFrame() throws IOException {
		Frame returnFrame = null;
		Packet packet = new Packet();
		
		while (packet.type != jpegPacket && packet.type != targaPacket) {
			packet = getPacket();
		}
		
		Image image = null;
		
		if (packet.type == jpegPacket) {
			WritableInputStream imageData = new WritableInputStream(packet.data, true);
			image = new Image(imageData.getInputStream());
		} else if (packet.type == targaPacket) {
			// TODO implement TARGA support
		}
		
		returnFrame = new Frame(image);
		
		return returnFrame;
	}
	
	public void sendNFCPatch(String host, byte[] addr, ConsoleModel model) throws UnknownHostException, IOException {
		byte[] binaryPacketPatch = new byte[11 + addr.length];
		binaryPacketPatch[0] = (byte) 0x81;
		binaryPacketPatch[1] = 0x0A;
		binaryPacketPatch[4] = (byte) ((model == ConsoleModel.N3DS) ? 0x1A : 0x19);
		binaryPacketPatch[binaryPacketPatch.length - 2] = 0x70;
		binaryPacketPatch[binaryPacketPatch.length - 1] = 0x47;
		
		Socket patchClient = new Socket(host, 6464);
		OutputStream patchOut = patchClient.getOutputStream();
		patchOut.write(binaryPacketPatch);
		patchOut.close();
		patchClient.close();
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
