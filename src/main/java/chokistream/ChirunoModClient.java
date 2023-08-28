package chokistream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
import chokistream.props.LogLevel;

/**
 * 
 */
public class ChirunoModClient implements StreamingInterface {
	
	private Socket client = null;
	private InputStream in = null;
	private OutputStream out = null;
	private ColorMode colorMode;
	public int quality;
	private BufferedImage lastTopImage;
	private BufferedImage lastBottomImage;
	
	private int topFrames;
	private int bottomFrames;
	
	private static final int FORMAT_MASK = 		0b00000111;
	private static final int TGA_MASK = 		0b00001000;
	private static final int SCREEN_MASK = 		0b00010000;
	private static final int INTERLACE_MASK = 	0b00100000;
	private static final int PARITY_MASK = 		0b01000000;
	
	private static final int FRACTIONAL_MASK = 	0b00001000;
	private static final int FRACTION_MASK = 	0b00000111;
	
	private static final Logger logger = Logger.INSTANCE;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 * @param colorMode The color filter (option to enable hotfixColors).
	 */
	public ChirunoModClient(String host, int quality, boolean reqTGA, boolean interlace, int capCPU, ColorMode receivedColorMode,
			int port, DSScreenBoth reqScreen) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, port);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
		this.colorMode = receivedColorMode;
		this.quality = quality;
		
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
		logger.log("Sending CPU limit packet of "+limitCPU, LogLevel.VERBOSE);
		out.write((new Packet((byte)0x04, (byte)0x02, new byte[] {(byte)limitCPU})).pack);
	}
	
	public void sendQuality(int quality) throws IOException {
		// Creates the quality packet to the 3DS
		logger.log("Sending quality packet of "+quality, LogLevel.VERBOSE);
		out.write((new Packet((byte)0x04, (byte)0x01, new byte[] {(byte)quality})).pack);
	}
	
	public void sendScreen(DSScreenBoth screen) throws IOException {
		logger.log("Sending screen packet of "+screen.getLongName(), LogLevel.VERBOSE);
		byte scr = switch(screen) {
			case TOP -> 0x01;
			case BOTTOM -> 0x02;
			case BOTH -> 0x03;
		};
		out.write((new Packet((byte)0x04, (byte)0x03, new byte[] {scr})).pack);
	}
	
	public void sendImageType(boolean isTGA) throws IOException {
		logger.log("Sending image type packet of "+(isTGA ? "TGA" : "JPEG"), LogLevel.VERBOSE);
		byte imtype = isTGA ? (byte)0x01 : (byte)0x00;
		out.write((new Packet((byte)0x04, (byte)0x04, new byte[] {imtype})).pack);	
	}
	
	public void sendInterlace(boolean interlace) throws IOException {
		logger.log("Sending interlace packet of "+interlace, LogLevel.VERBOSE);
		byte intl = interlace ? (byte)0x01 : (byte)0x00;
		out.write((new Packet((byte)0x04, (byte)0x05, new byte[] {intl})).pack);	
	}
	
	public void sendInit() throws IOException {
		logger.log("Sending init packet", LogLevel.VERBOSE);
		out.write((new Packet((byte)0x02, (byte)0x00, new byte[] {})).pack);
	}
	
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
		sendDisconnect();
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
		returnPacket.subtypeA = (byte)in.read();
		returnPacket.subtypeB = (byte)in.read();
		returnPacket.subtypeC = (byte)in.read();
		returnPacket.length = in.read() + (in.read() << 8) + (in.read() << 16) + (in.read() << 24);
		returnPacket.data = in.readNBytes(returnPacket.length);
		
		return returnPacket;
	}
	
	@Override
	public Frame getFrame() throws IOException {
		Frame returnFrame = null;
		
		// We currently loop through received packets until we get an image packet, which we then process
		// A long term answer is probably true asynchronous programming with callbacks
		do {
			Packet packet = new Packet();
			packet = getPacket();
			String pType = switch(packet.type) {
				case 0x01 -> "Image";
				case 0x02 -> "Init (unexpected!)";
				case 0x03 -> "Disconnect";
				case 0x04 -> "Settings (unexpected!)";
				case (byte) 0xFF -> "Debug";
				default -> "Unknown";
			};
			logger.log(String.format("Recieved packet of type 0x%02X (%s) and subtypes 0x%02X 0x%02X", packet.type, pType, packet.subtypeA, packet.subtypeB), LogLevel.VERBOSE);
			logger.log(""+packet.length, LogLevel.EXTREME);
			logger.log(packet.data, LogLevel.EXTREME);
			
			if(packet.type == 0x03) {
				// Might as well respect disconnect packets
				logger.log("Recieved disconnect packet, closing");
				close();
			} else if(packet.type == (byte)0xFF) {
				// Output debug packets at verbose level
				switch(packet.subtypeA) {
					case 0x01: // Binary
						logger.log(packet.data, LogLevel.REGULAR);
						break;
					case 0x02: // ASCII
						logger.log(new String(packet.data, StandardCharsets.US_ASCII), LogLevel.REGULAR);
						break;
					case 0x03: // UTF-8
						logger.log(new String(packet.data, StandardCharsets.UTF_8), LogLevel.REGULAR);
						break;
					default:
						// Log unknown format as binary
						logger.log(packet.data, LogLevel.REGULAR);
				}
			} else if(packet.type == 0x01) {
				// Image packet
				returnFrame = processImagePacket(packet);
			}
		} while(returnFrame == null); // If we get a null frame, try again
		
		return returnFrame;
	}
	
	private Frame processImagePacket(Packet packet) throws IOException {
		BufferedImage image = null;
		boolean lastFrame = true;
		
		DSScreen screen = (packet.subtypeA & SCREEN_MASK) > 0 ? DSScreen.BOTTOM : DSScreen.TOP;
		
		boolean rbSwap = false;
		
		if ((packet.subtypeA & TGA_MASK) == 0) { // JPEG mode
			image = ImageIO.read(new ByteArrayInputStream(packet.data));
			// For some reason the red and blue channels are swapped. Fix it.
			//image = ColorHotfix.doColorHotfix(image, colorMode, true);
			rbSwap = true;
		} else { // TGA mode
			image = TargaParser.parseBytes(packet.data, screen, TGAPixelFormat.fromInt(packet.subtypeA & FORMAT_MASK));
			//image = ColorHotfix.doColorHotfix(image, colorMode, false);
		}
		
		// Fix odd images in some CHM versions
		if(image.getWidth() == 256) {
			image = image.getSubimage(0, 0, 240, image.getHeight());
		}
		
		// Check if image dimensions are as expected
		int expWidth = (packet.subtypeA & INTERLACE_MASK) > 0 ? 120 : 240;
		int expHeight = (packet.subtypeA & SCREEN_MASK) > 0 ? 320 : 400;
		expHeight = (packet.subtypeB & FRACTIONAL_MASK) > 0 ? expHeight/8 : expHeight;
		if(image.getHeight() != expHeight || image.getWidth() != expWidth) {
			logger.log("Recieved incorrect dimensions! Expected "+expWidth+"x"+expHeight+", got "+image.getWidth()+"x"+image.getHeight());
			return null;
		}
		
		boolean interlace = (packet.subtypeA & INTERLACE_MASK) > 0; // Whether or not the image is interlaced
		int parity = packet.subtypeA & PARITY_MASK; // Interlace parity, ignored for non-interlaced images
		boolean fractional = (packet.subtypeB & FRACTIONAL_MASK) > 0; // Whether or not the image is fractional
		int fraction = fractional ? (packet.subtypeB & FRACTION_MASK) : 0; // Which fraction of the screen this is
		
		lastFrame = (!interlace || parity == 1) && (!fractional || (fraction+1)*image.getHeight() == 400);
		
		BufferedImage base = screen == DSScreen.TOP ? lastTopImage : lastBottomImage;
		image = ImageManipulator.adjust(base, image, interlace, parity, image.getHeight() * fraction, colorMode, rbSwap);
		
		if(screen == DSScreen.TOP) {
			lastTopImage = image;
		}  else {
			lastBottomImage = image;
		}
		
		// If we're not vsyncing, we still want to have correct fps, which means only count last frames
		if(lastFrame) {
			if(screen == DSScreen.TOP) {
				topFrames++;
			} else {
				bottomFrames++;
			}
		}
		
		return new Frame(screen, image);
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
	private class Packet {
		public byte type;
		public byte subtypeA;
		public byte subtypeB;
		public byte subtypeC;
		public int length;
		public byte[] data;
		public byte[] pack;
		
		Packet(){};
		
		Packet(byte type, byte subtype, byte[] data) {
			this.type = type;
			this.subtypeA = subtype;
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
			pack = new byte[length+8];
			pack[0] = type;
			pack[1] = subtypeA;
			pack[2] = subtypeB;
			pack[3] = subtypeC;
			pack[4] = (byte) length; // Narrowing ensures only bottom 8 bytes
			pack[5] = (byte)(length >>> 8);
			pack[6] = (byte)(length >>> 16);
			pack[7] = (byte)(length >>> 24);
			if(length > 0) {
				System.arraycopy(data, 0, pack, 8, length);
			}
		}
		
		void unpack() {
			type = pack[0];
			subtypeA = pack[1];
			subtypeB = pack[2];
			subtypeC = pack[3];
			length = pack[4] + (pack[5] << 8) + (pack[6] << 16) + (pack[7] << 24);
			data = Arrays.copyOfRange(pack, 9, length);
		}
	}
	
}
