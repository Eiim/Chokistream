package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 */
public class HZModClient implements StreamingInterface {
	
	private final int maxBytes = 1360;
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
		
		// Creates the initialization packet to the 3DS
		byte[] initializationPacket = new byte[9];
		initializationPacket[0] = 0x7E;
		initializationPacket[1] = 0x05;
		initializationPacket[8] = 0x01;
		
		out.write(initializationPacket);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public Frame getFrame() throws IOException {
		int imageSize = 0;
		int bytesRead = 0;
		byte[] packet = new byte[maxBytes];
		int socketBytesRead = in.read(packet);
		byte type = (socketBytesRead == 0) ? packet[0] : 0x00;
		while (socketBytesRead == 0 || type != targaPacket || type != jpegPacket) {
			packet = new byte[maxBytes];
			socketBytesRead = in.read(packet);
			type = (socketBytesRead == 0) ? packet[0] : 0x00;
		}
		// Convert LE to BE
		imageSize = (packet[3] << 16) + (packet[2] << 8) + packet[1];
		return null;
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
	
}
