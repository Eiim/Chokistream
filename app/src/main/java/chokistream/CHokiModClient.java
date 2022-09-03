package chokistream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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
public class CHokiModClient implements StreamingInterface {
	
	private Socket client = null;
	private InputStream in = null;
	private OutputStream out = null;
	private ColorMode colorMode;
	private double topScale;
	private double bottomScale;
	private InterpolationMode intrp;
	public int quality;
	private BufferedImage lastTopImage;
	private BufferedImage lastBottomImage;
	private boolean vsync;
	
	private static final int FORMAT_MASK = 		0b00000111;
	private static final int TGA_MASK = 		0b00001000;
	private static final int SCREEN_MASK = 		0b00010000;
	private static final int INTERLACE_MASK = 	0b00100000;
	private static final int PARITY_MASK = 		0b01000000;
	
	private static final Logger logger = Logger.INSTANCE;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 */
	public CHokiModClient(String host, int quality, boolean reqTGA, boolean interlace, boolean vsync, int capCPU, ColorMode receivedColorMode,
			int port, DSScreenBoth reqScreen, double topScale, double bottomScale, InterpolationMode intrp) throws UnknownHostException, IOException {
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
		this.vsync = vsync;
		
		lastTopImage = new BufferedImage(400, 240, BufferedImage.TYPE_INT_RGB);
		lastBottomImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		
		if (capCPU > 0) {
			sendLimitCPU(capCPU);
		}
		sendImageType(reqTGA);
		if (!reqTGA) {
			sendQuality(quality);
		}
		sendInterlace(interlace);
		sendScreen(reqScreen);
		sendInit();
	}
	
	public void sendLimitCPU(int limitCPU) throws IOException {
		// Creates the limit CPU packet to the 3DS
		logger.log("Sending CPU limit packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0x04, (byte)0x02, new byte[] {(byte)limitCPU})).pack);
	}
	
	public void sendQuality(int quality) throws IOException {
		// Creates the quality packet to the 3DS
		logger.log("Sending quality packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0x04, (byte)0x01, new byte[] {(byte)quality})).pack);
	}
	
	public void sendScreen(DSScreenBoth screen) throws IOException {
		logger.log("Sending screen packet", LogLevel.VERBOSE);
		byte scr = switch(screen) {
			case TOP -> 0x01;
			case BOTTOM -> 0x02;
			case BOTH -> 0x03;
		};
		out.write((new Packet((byte)0x04, (byte)0x03, new byte[] {scr})).pack);
	}
	
	public void sendImageType(boolean isTGA) throws IOException {
		logger.log("Sending image type packet", LogLevel.VERBOSE);
		byte imtype = isTGA ? (byte)0x01 : (byte)0x00;
		out.write((new Packet((byte)0x04, (byte)0x04, new byte[] {imtype})).pack);	
	}
	
	public void sendInterlace(boolean interlace) throws IOException {
		logger.log("Sending interlace packet", LogLevel.VERBOSE);
		byte intl = interlace ? (byte)0x01 : (byte)0x00;
		out.write((new Packet((byte)0x04, (byte)0x05, new byte[] {intl})).pack);	
	}
	
	public void sendInit() throws IOException {
		// This is really just 02 00 00 00 00
		logger.log("Sending init packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0x02, (byte)0x00, new byte[] {})).pack);
	}
	
	// We don't really have a use for this yet but might as well support it
	public void sendDisconnect() throws IOException {
		logger.log("Sending disconnect packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0x03, (byte)0x00, new byte[] {})).pack);
	}
	
	// We don't really have a use for this yet but might as well support it
	public void sendDebug(byte[] debugData) throws IOException {
		logger.log("Sending debug packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0xFF, (byte)0x00, debugData)).pack);
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
		returnPacket.subtype = (byte)in.read();
		returnPacket.length = in.read() + (in.read() << 8) + (in.read() << 16) + (in.read() << 24);
		returnPacket.data = in.readNBytes(returnPacket.length);
		
		return returnPacket;
	}
	
	@Override
	public Frame getFrame() throws IOException {
		Frame returnFrame = null;
		Packet packet = new Packet();
		
		// I'd kinda like to refactor this loop but it works for now
		while (packet.type != 0x01) { // 0x01 is Image packet
			packet = getPacket();
			String pType = switch(packet.type) {
				case 0x01 -> "Image";
				case 0x02 -> "Init (unexpected!)";
				case 0x03 -> "Disconnect";
				case 0x04 -> "Settings (unexpected!)";
				case (byte) 0xFF -> "Debug";
				default -> "Unknown";
			};
			logger.log(String.format("Recieved packet of type 0x%02X (%s) and subtype 0x%02X", packet.type, pType, packet.subtype), LogLevel.VERBOSE);
			logger.log(""+packet.length, LogLevel.EXTREME);
			logger.log(packet.data, LogLevel.EXTREME);
			
			if(packet.type == 0x03) {
				// Might at a well respect disconnect packets
				logger.log("Recieved disconnect packet, closing");
				close();
			} else if(packet.type == (byte)0xFF) {
				// Output debug packets at verbose level
				switch(packet.subtype) {
					case 0x01: // Binary
						logger.log(packet.data, LogLevel.VERBOSE);
						break;
					case 0x02: // ASCII
						logger.log(new String(packet.data, StandardCharsets.US_ASCII), LogLevel.VERBOSE);
						break;
					case 0x03: // UTF-8
						logger.log(new String(packet.data, StandardCharsets.UTF_8), LogLevel.VERBOSE);
						break;
					default:
						// Don't log for now
				}
			}
		}
		
		// Whoa, we have a sane way to do screen detection now? How crazy!
		DSScreen screen = (packet.subtype & SCREEN_MASK) > 0 ? DSScreen.BOTTOM : DSScreen.TOP;
		
		BufferedImage image = null;
		
		if ((packet.subtype & TGA_MASK) == 0) { // JPEG mode
			WritableInputStream imageData = new WritableInputStream(packet.data, true);
			image = ImageIO.read(imageData.getInputStream());
			// For some reason the red and blue channels are swapped. Fix it.
			image = ColorHotfix.doColorHotfix(image, colorMode, true);
		} else { // TGA mode
			image = TargaParser.parseBytes(packet.data, screen, TGAPixelFormat.fromInt(packet.subtype & FORMAT_MASK));
			image = ColorHotfix.doColorHotfix(image, colorMode, false);
		}
		
		// Interlace with last frame, if applicable.
		if((packet.subtype & INTERLACE_MASK) > 0) {
			if(screen == DSScreen.TOP) {
				image = interlace(lastTopImage, image, (packet.subtype & PARITY_MASK)/PARITY_MASK);
			}  else {
				image = interlace(lastBottomImage, image, (packet.subtype & PARITY_MASK)/PARITY_MASK);
			}
		}
		if(screen == DSScreen.TOP) {
			lastTopImage = image;
		}  else {
			lastBottomImage = image;
		}
		
		image = Interpolator.scale(image, intrp, screen == DSScreen.BOTTOM ? bottomScale : topScale);
		
		returnFrame = new Frame(screen, image);
		
		return returnFrame;
	}
	
	/* 
	 * It's really the *columns* of the image that are interlaced, which correspond to the *rows* of the screen.
	 */
	private BufferedImage interlace(BufferedImage oldIm, BufferedImage newIm, int parity) {
		int height = oldIm.getHeight();
		for(int col = 0; col < 120; col++) {
			for(int row = 0; row < height; row++) {
				oldIm.setRGB(col*2+parity, row, newIm.getRGB(col, row));
			}
		}
		return oldIm;
	}
	
	/**
	 * Represents a packet received from HzMod
	 */
	private class Packet {
		public byte type;
		public byte subtype;
		public int length;
		public byte[] data;
		public byte[] pack;
		
		Packet(){};
		
		Packet(byte type, byte subtype, byte[] data) {
			this.type = type;
			this.subtype = subtype;
			this.length = data.length;
			this.data = data;
			pack();
		}
		
		// This seems like something we should have but I don't have a use for it
		@SuppressWarnings("unused")
		Packet(byte[] rawData) {
			pack = rawData;
			unpack();
		}
		
		void pack() {
			pack = new byte[length+6];
			pack[0] = type;
			pack[1] = subtype;
			pack[2] = (byte) length; // Narrowing ensures only bottom 8 bytes
			pack[3] = (byte)(length >>> 8);
			pack[4] = (byte)(length >>> 16);
			pack[5] = (byte)(length >>> 24);
			if(length > 0) {
				System.arraycopy(data, 0, pack, 6, length);
			}
		}
		
		void unpack() {
			type = pack[0];
			subtype = pack[1];
			length = pack[2] + (pack[3] << 8) + (pack[4] << 16) + (pack[4] << 24);
			data = Arrays.copyOfRange(pack, 5, length);
		}
	}
	
}
