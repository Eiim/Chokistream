package chokistream;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
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
	private BufferedImage lastTopImage;
	private BufferedImage lastBottomImage;
	
	private int topFrames;
	private int bottomFrames;
	
	// Remember settings for future use
	private int quality;
	private int cpuLimit;
	private boolean tga;
	private boolean interlace;
	private DSScreenBoth screen;
	private final ColorMode colorMode; // probably no need to have this modifiable
	
	private static final int FORMAT_MASK = 		0b00000111;
	private static final int TGA_MASK = 		0b00001000;
	private static final int SCREEN_MASK = 		0b00010000;
	private static final int INTERLACE_MASK = 	0b00100000;
	private static final int PARITY_MASK = 		0b01000000;
	
	private static final int FRACTIONAL_MASK = 	0b00001000;
	private static final int FRACTION_MASK = 	0b00000111;
	
	private static final Logger logger = Logger.INSTANCE;

	/**
	 * Create a ChirunoModClient.
	 * @param host The host or IP to connect to
	 * @param quality The quality to stream at
	 * @param reqTGA Whether to request TGA frames
	 * @param capCPU Cap CPU cycles
	 * @param colorMode The color filter for hotfixing colors
	 * @param port The 3DS port
	 * @param reqScreen The 3DS screen to request
	 */
	public ChirunoModClient(String host, int quality, boolean reqTGA, boolean interlace, int capCPU, ColorMode receivedColorMode,
			int port, DSScreenBoth reqScreen) throws UnknownHostException, IOException {
		this.quality = quality;
		this.cpuLimit = capCPU;
		this.tga = reqTGA;
		this.screen = reqScreen;
		this.interlace = interlace;
		
		// Connect to TCP port and set up client
		client = new Socket(host, port);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
		
		this.colorMode = receivedColorMode;
		
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
		// experimental hack; un-comment these lines if you want
		//logger.log("Setting Priority Factor to 8");
		//int priorityFactor = 8;
		//out.write((new Packet((byte)0x04, (byte)0x07, new byte[] {(byte)priorityFactor})).pack);
		//logger.log("Setting \"GBVC Mode\" to Force-Enable");
		//out.write((new Packet((byte)0x04, (byte)0x0B, new byte[] {(byte)2})).pack);
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
	
	// Decrease quality by a certain amount, down to 1
	public void decreaseQuality(int delta) throws IOException {
		if(quality - delta > 1) {
			quality = quality - delta;
			sendQuality(quality);
		} else if(quality > 1) {
			quality = 1;
			sendQuality(1);
		}
	}
	
	// Increase CPU cap by a certain amount, up to 100
	public void increaseCPU(int delta) throws IOException {
		if(cpuLimit + delta < 100) {
			cpuLimit = cpuLimit + delta;
			sendLimitCPU(cpuLimit);
		} else if(cpuLimit < 100) {
			cpuLimit = 100;
			sendLimitCPU(100);
		}
	}
	
	// Decrease CPU cap by a certain amount, down to 0
	public void decreaseCPU(int delta) throws IOException {
		if(cpuLimit - delta > 0) {
			cpuLimit = cpuLimit - delta;
			sendLimitCPU(cpuLimit);
		} else if(cpuLimit > 0) {
			cpuLimit = 0;
			sendLimitCPU(0);
		}
	}
	
	public void toggleTGA() throws IOException {
		tga = !tga;
		sendImageType(tga);
	}
	
	public void toggleInterlacing() throws IOException {
		interlace = !interlace;
		sendInterlace(interlace);
	}
	
	public void switchScreen() throws IOException {
		screen = switch(screen) {
			case TOP -> DSScreenBoth.BOTTOM;
			case BOTTOM -> DSScreenBoth.BOTH;
			case BOTH -> DSScreenBoth.TOP;
		};
		sendScreen(screen);		
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
			Packet packet = getPacket();
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
			try {
				image = ImageIO.read(new ByteArrayInputStream(packet.data));
			} catch (IOException e) {
				logger.log("processImagePacket warning: ImageIO.read() threw an exception during JPEG decoding:", LogLevel.REGULAR);
				logger.log(""+e.getClass()+": "+e.getMessage()+System.lineSeparator()+Arrays.toString(e.getStackTrace()), LogLevel.REGULAR);
			}
			// For some reason the red and blue channels are swapped. Fix it.
			rbSwap = true;
		} else { // TGA mode
			image = TargaParser.parseBytes(packet.data, screen, TGAPixelFormat.fromInt(packet.subtypeA & FORMAT_MASK));
		}
		
		if(image == null)
		{
			logger.log("processImagePacket error: \"image\" is null");
			return null;
		}
		
		// Fix odd images in some CHM versions
		if(image.getWidth() == 256) {
			image = image.getSubimage(0, 0, 240, image.getHeight());
		}
		else if(image.getWidth() == 128) {
			image = image.getSubimage(0, 0, 120, image.getHeight());
		}
		
		boolean interlace = (packet.subtypeA & INTERLACE_MASK) > 0; // Whether or not the image is interlaced
		int parity = (packet.subtypeA & PARITY_MASK) / PARITY_MASK; // Interlace parity, ignored for non-interlaced images
		boolean fractional = (packet.subtypeB & FRACTIONAL_MASK) > 0; // Whether or not the image is fractional
		int fraction = fractional ? (packet.subtypeB & FRACTION_MASK) : 0; // Which fraction of the screen this is
		
		int offsY = 0;
		int offsX = 0;
		if ((packet.subtypeA & TGA_MASK) != 0) {
			offsY = (packet.data[10] & 0xff) + ((packet.data[11] & 0xff) << 8); // origin_y
			offsX = (packet.data[8] & 0xff) + ((packet.data[9] & 0xff) << 8); // origin_x
			logger.log("offsY="+offsY+", offsX="+offsX, LogLevel.VERBOSE);
		} else { // JPEG
			// fallback to current-spec compliant method
			offsY = image.getHeight() * fraction;
			offsX = 0;
			// TODO: potential new feature of ChirunoMod spec. not yet though.
			// First two bytes is pixel offset (actually four, but other two are never used)
			//offsY = (packet.data[0] & 0xff) + ((packet.data[1] & 0xff) << 8);
			//offsX = (packet.data[2] & 0xff) + ((packet.data[3] & 0xff) << 8);
		}
		// sanity check
		if(offsY >= 720) {
			logger.log("processImagePacket warning: offsY="+offsY, LogLevel.REGULAR);
			offsY = 0;
		}
		if(offsX >= 240) {
			logger.log("processImagePacket warning: offsX="+offsX, LogLevel.REGULAR);
			offsX = 0;
		}
		
		// Check if image dimensions are as expected
		int expWidth = interlace ? 120 : 240;
		int expHeight = screen == DSScreen.BOTTOM ? 320 : 400;
		expHeight = fractional ? expHeight/8 : expHeight;
		
		if(image.getWidth() == 1 || image.getWidth() == 2) {
			// known invalid
			logger.log("processImagePacket warning: image.getWidth() == "+image.getWidth()+" (known invalid)", LogLevel.VERBOSE);
			return null;
		}
		
		if(image.getHeight() != expHeight || image.getWidth() != expWidth) {
			/* (TODO) if(imgIsGbvcMode) then these are also known valid image dimensions */
			if(true)
			{
				if(image.getHeight() != 160 || image.getWidth() != 144) {
					logger.log("Received incorrect dimensions! Expected "+expWidth+"x"+expHeight+", got "+image.getWidth()+"x"+image.getHeight());
					//return null;
				}
			} else {
				logger.log("Received incorrect dimensions! Expected "+expWidth+"x"+expHeight+", got "+image.getWidth()+"x"+image.getHeight());
				//return null;
			}
		}
		
		// Check if this is the end of the frame (if interlacing, is it the second interlace? if fractional, is it the last fraction?)
		lastFrame = (!interlace || parity == 1) && (!fractional || fraction == 7);
		
		BufferedImage base = screen == DSScreen.TOP ? lastTopImage : lastBottomImage;
		
		//rbSwap = false; // HACK
		
		/**
		 * TODO: This is a very dirty hack, for now. I'll implement this as a proper feature soon.
		 * 
		 * We don't yet have a setting or flag to signal this special type of image, so this hack
		 * just treats all TGA images as if they were the special type.
		 * 
		 * Er, change of plans because I'm feeling extra lazy.
		 * If it's a JPEG, a top screen frame, and in RGB5A1 format, 
		 * then carve a cutout so we don't cover up the pristine TGA GB screen with a crappy JPEG.
		 */
		if((TGAPixelFormat.fromInt(packet.subtypeA & FORMAT_MASK) == TGAPixelFormat.RGB5A1) 
		 && (screen == DSScreen.TOP) && ((packet.subtypeA & TGA_MASK) == 0)) {
			int step = 1;
			// additional note: i don't care enough to support interlacing (...yet...?)
			try {
				// cut the JPEG into pieces so it doesn't overlap the GB screen
				// (hilariously inefficient)
				BufferedImage image1 = image.getSubimage(0, 0, 240, 120);
				step++;
				BufferedImage image2 = image.getSubimage(0, 120, 48, 160);
				step++;
				BufferedImage image3 = image.getSubimage(192, 120, 48, 160);
				step++;
				BufferedImage image4 = image.getSubimage(0, 280, 240, 120);
				step++;
				
				image = ImageManipulator.adjust(base, image1, interlace, parity, 0, 0, colorMode, rbSwap);
				step++;
				image = ImageManipulator.adjust(base, image2, interlace, parity, 120, 192, colorMode, rbSwap);
				step++;
				image = ImageManipulator.adjust(base, image3, interlace, parity, 120, 0, colorMode, rbSwap);
				step++;
				image = ImageManipulator.adjust(base, image4, interlace, parity, 280, 0, colorMode, rbSwap);
				
			} catch(RasterFormatException e) {
				logger.log("processImagePacket warning: Exception thrown by GBVC JPEG background cutout related code, during step "+step+":", LogLevel.REGULAR);
				logger.log(""+e.getClass()+": "+e.getMessage(), LogLevel.REGULAR);
				logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
				if(step <= 5) {
					// fallback, as new image buffer has not yet been written to.
					logger.log("Proceeding with fallback ImageManipulator.adjust() for this frame.");
					image = ImageManipulator.adjust(base, image, interlace, parity, offsY, offsX, colorMode, rbSwap);
				}
			}
		}
		else
		{
			image = ImageManipulator.adjust(base, image, interlace, parity, offsY, offsX, colorMode, rbSwap);
		}
		
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
	private static class Packet {
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
