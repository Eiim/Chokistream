package chokistream;

import java.awt.image.BufferedImage;
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
import chokistream.props.InterpolationMode;
import chokistream.props.LogLevel;

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
	private double topScale;
	private double bottomScale;
	private InterpolationMode intrp;
	public int quality;
	private TGAPixelFormat topFormat = TGAPixelFormat.BITS_24;
	private TGAPixelFormat bottomFormat = TGAPixelFormat.BITS_24;
	
	private static final Logger logger = Logger.INSTANCE;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 */
	public HZModClient(String host, int quality, int capCPU, ColorMode receivedColorMode, int port, DSScreenBoth reqScreen,
			double topScale, double bottomScale, InterpolationMode intrp) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, port);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
		colorMode = receivedColorMode;
		this.topScale = topScale;
		this.bottomScale = bottomScale;
		this.intrp = intrp;
		this.quality = quality;
		
		// I believe these values are correct based on the HorizonScreen source code
		byte screenByte = switch(reqScreen) {
			case TOP -> 0x01;
			case BOTTOM -> 0x02;
			case BOTH -> 0x03;
		};
		
		if (capCPU > 0) {
			sendLimitCPU(capCPU);
		}
		
		sendQuality(quality);
		
		sendInit(screenByte);
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
		
		while (packet.type != jpegPacket && packet.type != targaPacket) {
			packet = getPacket();
			String pType = switch(packet.type) {
				case jpegPacket -> "JPEG";
				case targaPacket -> "TGA";
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
				bottomFormat = TGAPixelFormat.fromInt(packet.data[2] & 0x07);
				logger.log("Set top TGA pixel format to "+topFormat, LogLevel.VERBOSE);
				logger.log("Set bottom TGA pixel format to "+bottomFormat, LogLevel.VERBOSE);
			} else if(packet.type == 0x01) {
				// Might at a well respect disconnect packets
				logger.log("Recieved disconnect packet, closing");
				close();
			}
		}
		
		// Bottom packets start with 90 01
		DSScreen screen = packet.data[1] > 0 ? DSScreen.BOTTOM : DSScreen.TOP;
		/*
		 * No clue why, but HzMod includes an extra 8 bytes at the front of the image.
		 * We need to trim it off.
		 */
		byte[] data = Arrays.copyOfRange(packet.data, 8, packet.data.length);
		
		BufferedImage image = null;
		
		if (packet.type == jpegPacket) {
			WritableInputStream imageData = new WritableInputStream(data, true);
			image = ImageIO.read(imageData.getInputStream());
			// For some reason the red and blue channels are swapped. Fix it.
			image = ColorHotfix.doColorHotfix(image, colorMode, true);
		} else if (packet.type == targaPacket) {
			image = TargaParser.parseBytes(data, screen, screen == DSScreen.BOTTOM ? bottomFormat : topFormat);
			image = ColorHotfix.doColorHotfix(image, colorMode, false);
		}
		
		image = Interpolator.scale(image, intrp, screen == DSScreen.BOTTOM ? bottomScale : topScale);
		
		returnFrame = new Frame(screen, image);
		
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
