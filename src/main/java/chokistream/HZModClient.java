package chokistream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

/**
 * 
 */
public class HZModClient implements StreamingInterface {
	
	private static final byte TARGA_PACKET = 0x03;
	private static final byte JPEG_PACKET = 0x04;
	
	private Socket client = null;
	private InputStream in = null;
	private OutputStream out = null;
	private ColorMode colorMode;
	public int quality;
	private TGAPixelFormat topFormat = TGAPixelFormat.RGB8;
	private TGAPixelFormat bottomFormat = TGAPixelFormat.RGB8;
	private BufferedImage lastTopImage;
	private BufferedImage lastBottomImage;
	
	private int topFrames;
	private int bottomFrames;
	
	private static final Logger logger = Logger.INSTANCE;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 */
	public HZModClient(String host, int quality, int capCPU, ColorMode receivedColorMode, int port) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, port);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
		colorMode = receivedColorMode;
		this.quality = quality;
		
		lastTopImage = new BufferedImage(240, 400, BufferedImage.TYPE_INT_RGB);
		lastBottomImage = new BufferedImage(240, 320, BufferedImage.TYPE_INT_RGB);
		
		if (capCPU > 0) {
			sendLimitCPU(capCPU);
		}
		
		sendQuality(quality);
		
		sendInit((byte)0x01);
	}
	
	public void sendLimitCPU(int limitCPU) throws IOException {
		// Creates the limit CPU packet to the 3DS
		byte[] limitCPUPacket = new byte[9];
		limitCPUPacket[0] = 0x7E;
		limitCPUPacket[1] = 0x05;
		limitCPUPacket[4] = (byte) 0xFF;
		limitCPUPacket[8] = (byte) limitCPU;
		logger.log("Sending limit CPU packet", LogLevel.EXTREME);
		logger.log(limitCPUPacket, LogLevel.EXTREME);
		out.write(limitCPUPacket);
	}
	
	public void sendQuality(int quality) throws IOException {
		// Creates the quality packet to the 3DS
		byte[] qualityPacket = new byte[9];
		qualityPacket[0] = 0x7E;
		qualityPacket[1] = 0x05;
		qualityPacket[4] = 0x03;
		qualityPacket[8] = (byte) quality;
		logger.log("Sending quality packet", LogLevel.EXTREME);
		logger.log(qualityPacket, LogLevel.EXTREME);
		out.write(qualityPacket);
	}
	
	// Increase quality by a certain amount, up to 100
	public void increaseQuality(int delta) throws IOException {
		if(quality + delta < 100) {
			quality = quality + delta;
			sendQuality(quality);
		} else if(quality < 100) {
			quality = 100;
			sendQuality(100);
		}
	}
	
	// Decrease quality by a certain amount, down to 0
	public void decreaseQuality(int delta) throws IOException {
		if(quality - delta > 0) {
			quality = quality - delta;
			sendQuality(quality);
		} else if(quality > 0) {
			quality = 0;
			sendQuality(0);
		}
	}
	
	/**
	 * You shouldn't really ever need to call this outside of the constructor, but
	 * you can in case you really want to.
	 * @param screen 0x01*top | 0x02*bottom (Really, just send 1, or maybe sometimes 3)
	 * @throws IOException
	 */
	public void sendInit(byte screen) throws IOException {
		// Creates the initialization packet to the 3DS
		byte[] initializationPacket = new byte[9];
		initializationPacket[0] = 0x7E;
		initializationPacket[1] = 0x05;
		initializationPacket[8] = screen;
		logger.log("Sending initialization packet", LogLevel.EXTREME);
		logger.log(initializationPacket, LogLevel.EXTREME);
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
		
		int type = in.read();
		if(type == -1) {
			throw new SocketException("Socket closed");
		}
		returnPacket.type = (byte) type;
		returnPacket.length = in.read() + (in.read() << 8) + (in.read() << 16);
		returnPacket.data = in.readNBytes(returnPacket.length);
		
		return returnPacket;
	}
	
	@Override
	public Frame getFrame() throws IOException {
		Frame returnFrame = null;
		Packet packet = new Packet();
		
		while (packet.type != JPEG_PACKET && packet.type != TARGA_PACKET) {
			packet = getPacket();
			String pType = switch(packet.type) {
				case JPEG_PACKET -> "JPEG";
				case TARGA_PACKET -> "TGA";
				case 0x01 -> "Disconnect";
				case 0x02 -> "Set mode";
				case 0x7E -> "CFGBLK";
				case (byte) 0xFF -> "Debug";
				default -> "Unknown";
			};
			logger.log(String.format("Recieved packet of type 0x%02X (Type %s)", packet.type, pType), LogLevel.VERBOSE);
			logger.log(""+packet.length, LogLevel.EXTREME);
			logger.log(packet.data, LogLevel.EXTREME);
			
			// If we get a Set Mode packet, we need to update our pixel format
			if(packet.type == 0x02) {
				topFormat = TGAPixelFormat.fromInt(packet.data[0] & 0x07);
				bottomFormat = TGAPixelFormat.fromInt(packet.data[8] & 0x07);
				logger.log("Set top TGA pixel format to "+topFormat, LogLevel.VERBOSE);
				logger.log("Set bottom TGA pixel format to "+bottomFormat, LogLevel.VERBOSE);
				
				// Log to console if a "Stride" value is unexpected. (debug)
				int topStride = (packet.data[4]&0xff) + ((packet.data[5]&0xff)*0x100) + ((packet.data[6]&0xff)*0x10000) + ((packet.data[7]&0xff)*0x1000000);
				if(topStride / topFormat.bytes != 240)
					logger.log("Warning: Unexpected \"Stride\" for top screen. stride="+topStride+"; possible-width="+(topStride/topFormat.bytes), LogLevel.VERBOSE);
				int botStride = (packet.data[12]&0xff) + ((packet.data[13]&0xff)*0x100) + ((packet.data[14]&0xff)*0x10000) + ((packet.data[15]&0xff)*0x1000000);
				if(botStride / bottomFormat.bytes != 240)
					logger.log("Warning: Unexpected \"Stride\" for bottom screen. stride="+botStride+"; possible-width="+(botStride/bottomFormat.bytes), LogLevel.VERBOSE);
				
			} else if(packet.type == 0x01) {
				// Might as well respect disconnect packets
				logger.log("Recieved disconnect packet, closing");
				close();
			}
		}
		
		int offset;
		
		if (packet.type == TARGA_PACKET) {
			offset = (packet.data[10] & 0xff) + ((packet.data[11] & 0xff) << 8); // origin_y
		} else { // JPEG
			// First two bytes is pixel offset (actually four, but other two are never used)
			offset = (packet.data[0] & 0xff) + ((packet.data[1] & 0xff) << 8);
		}
		
		logger.log("offset="+offset, LogLevel.EXTREME);
		
		if(offset < 0 || offset > 720) {
			offset = 0;
		}
		
		// Values >= 400 indicate bottom screen
		DSScreen screen = offset >= 400 ? DSScreen.BOTTOM : DSScreen.TOP;
		
		// If bottom screen, subtract 400 to get actual offset
		offset %= 400;
		
		// Trim extra header (added by HzMod) if necessary
		int packetDataOffset = 0;
		if (packet.type == JPEG_PACKET) {
			packetDataOffset = 8;
		}
		
		byte[] data = Arrays.copyOfRange(packet.data, packetDataOffset, packet.data.length);
		
		BufferedImage image = null;
		boolean rbSwap = false;
		
		if (packet.type == JPEG_PACKET) {
			image = ImageIO.read(new ByteArrayInputStream(data));
			// For some reason the red and blue channels are swapped. Fix it.
			rbSwap = true;
		} else if (packet.type == TARGA_PACKET) {
			image = TargaParser.parseBytes(data, screen, screen == DSScreen.BOTTOM ? bottomFormat : topFormat);
		}
		
		// Fix odd images in some games
		if(image.getWidth() == 256) {
			image = image.getSubimage(0, 0, 240, image.getHeight());
		}
		
		if(screen == DSScreen.BOTTOM) {
			if(image.getHeight() == 320) { // Full screen
				bottomFrames++;
				lastBottomImage = ImageManipulator.adjust(image, colorMode, rbSwap);
			} else if(image.getWidth() > 1) { // Fractional
				if(image.getHeight() + offset == 320) bottomFrames++;
				lastBottomImage = ImageManipulator.adjust(lastBottomImage, image, false, 0, offset, colorMode, rbSwap);;
			} else {
				// 1-wide frame, use the last one instead
			}
			returnFrame = new Frame(DSScreen.BOTTOM, lastBottomImage);
		} else {
			if(image.getHeight() == 400) { // Full screen
				topFrames++;
				lastTopImage = ImageManipulator.adjust(image, colorMode, rbSwap);
			} else if(image.getWidth() > 1) { // Fractional
				if(image.getHeight() + offset == 400) topFrames++;
				lastTopImage = ImageManipulator.adjust(lastTopImage, image, false, 0, offset, colorMode, rbSwap);
			} else {
				// 1-wide frame, use the last one instead
			}
			returnFrame = new Frame(DSScreen.TOP, lastTopImage);
		}
		
		return returnFrame;
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
	
	/**
	 * Represents a packet received from HzMod
	 */
	private static class Packet {
		public byte type;
		public int length;
		public byte[] data;
	}
	
}
