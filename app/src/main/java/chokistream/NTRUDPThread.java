package chokistream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;
import chokistream.props.LogLevel;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * 
 */
public class NTRUDPThread extends Thread {
	
	/**
	 * Socket used to receive UDP packets from the 3DS on.
	 */
	private DatagramSocket socket;
	
	/**
	 * Buffer used to store UDP data in.
	 */
	private byte[] packetBuffer = new byte[1448];
	
	/**
	 * A BlockingQueue to buffer received Frames in.
	 */
	private BlockingQueue<Frame> frameBuffer = new LinkedBlockingQueue<Frame>();
	
	/**
	 * Should the thread die?
	 */
	private AtomicBoolean shouldDie = new AtomicBoolean(false);
	
	WritableInputStream priorityInputStream = new WritableInputStream();
	WritableInputStream secondaryInputStream = new WritableInputStream();
	private BufferedImage priorityImage;
	private BufferedImage secondaryImage;
	private byte priorityExpectedFrame = 0;
	private byte secondaryExpectedFrame = 0;
	private byte priorityExpectedPacket = 0;
	private byte secondaryExpectedPacket = 0;
	private DSScreen activeScreen = DSScreen.TOP;
	private ColorMode colorMode;
	private double topScale;
	private double bottomScale;
	private InterpolationMode intrp;
	
	private static final Logger logger = Logger.INSTANCE;
	
	/**
	 * Create an NTRUDPThread.
	 * @param host The host or IP to connect to.
	 * @throws SocketException
	 */
	NTRUDPThread(String host, DSScreen screen, ColorMode colorMode, double topScale, double bottomScale, InterpolationMode intrp) throws SocketException {
		activeScreen = screen;
		socket = new DatagramSocket(8001);
		this.colorMode = colorMode;
		this.topScale = topScale;
		this.bottomScale = bottomScale;
		this.intrp = intrp;
	}
	
	public Frame getFrame() throws InterruptedException {
		return frameBuffer.take();
	}
	
	public void close() {
		socket.close();
	}

	public void run() {
		while (!shouldDie.get()) {
			DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
			try {
				socket.receive(packet);
				byte[] data = packet.getData();
				byte currentFrame = data[0];
				DSScreen currentScreen = ((data[1] & 0x0F) == 1) ? DSScreen.TOP : DSScreen.BOTTOM;
				boolean isLastPacket = (((data[1] & 0xF0) >> 4) == 1);
				int currentPacket = data[3];
				
				logger.log("Recieved packet for screen "+currentScreen.getLongName()+
						", isLast="+isLastPacket+", curF="+currentFrame+", curP="+currentPacket, LogLevel.VERBOSE);
				logger.log(data, LogLevel.EXTREME);
				
				if (priorityExpectedFrame == 0 && currentScreen == activeScreen) {
					priorityExpectedFrame = currentFrame;
				} else if (secondaryExpectedFrame == 0) {
					secondaryExpectedFrame = currentFrame;
				}
				
				if (priorityExpectedFrame == currentFrame && priorityExpectedPacket == currentPacket && activeScreen == currentScreen) {
					// Priority screen
					byte[] dataToWrite = Arrays.copyOfRange(data, 4, data.length);
					priorityInputStream.write(dataToWrite);
					priorityExpectedPacket++;
					
					if (isLastPacket) {
						priorityInputStream.markFinishedWriting();
						priorityImage = ImageIO.read(priorityInputStream.getInputStream());
						priorityInputStream = new WritableInputStream();
						if (colorMode != ColorMode.REGULAR) {
							priorityImage = ColorHotfix.doColorHotfix(priorityImage, colorMode, false);
						}
						if(currentScreen == DSScreen.TOP) {
							priorityImage = Interpolator.scale(priorityImage, intrp, topScale);
						} else {
							priorityImage = Interpolator.scale(priorityImage, intrp, bottomScale);
						}
						frameBuffer.add(new Frame(currentScreen, priorityImage));
						priorityImage = null;
						priorityExpectedFrame = 0;
	                    priorityExpectedPacket = 0;
					}
				} else if (currentScreen == activeScreen) {
					// Unexpected priority packet or frame
					priorityInputStream = new WritableInputStream();
					priorityImage = null;
					priorityExpectedFrame = 0;
                    priorityExpectedPacket = 0;
				} else if(secondaryExpectedPacket == currentPacket) {
					// Secondary screen
					byte[] dataToWrite = Arrays.copyOfRange(data, 4, data.length);
					secondaryInputStream.write(dataToWrite);
					secondaryExpectedPacket++;
					
					if (isLastPacket) {
						secondaryInputStream.markFinishedWriting();
						secondaryImage = ImageIO.read(secondaryInputStream.getInputStream());
						secondaryInputStream = new WritableInputStream();
						if (colorMode != ColorMode.REGULAR) {
							secondaryImage = ColorHotfix.doColorHotfix(secondaryImage, colorMode, false);
						}
						if(currentScreen == DSScreen.TOP) {
							secondaryImage = Interpolator.scale(secondaryImage, intrp, topScale);
						} else {
							secondaryImage = Interpolator.scale(secondaryImage, intrp, bottomScale);
						}
						frameBuffer.add(new Frame(currentScreen, secondaryImage));
						secondaryImage = null;
						secondaryExpectedFrame = 0;
	                    secondaryExpectedPacket = 0;
					}
				} else {
					// Unexpected secondary packet or frame
					secondaryInputStream = new WritableInputStream();
					secondaryImage = null;
					secondaryExpectedFrame = 0;
                    secondaryExpectedPacket = 0;
				}
			} catch (IOException e) {
				shouldDie.set(true);
				socket.close();
				e.printStackTrace();
			}
		}
	}
	
}
