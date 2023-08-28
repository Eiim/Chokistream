package chokistream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.LogLevel;

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
	
	//WritableInputStream priorityInputStream = new WritableInputStream();
	//WritableInputStream secondaryInputStream = new WritableInputStream();
	byte[] priorityImageData = new byte[0];
	byte[] secondaryImageData = new byte[0];
	private BufferedImage priorityImage;
	private BufferedImage secondaryImage;
	private byte priorityExpectedFrame = 0;
	private byte secondaryExpectedFrame = 0;
	private byte priorityExpectedPacket = 0;
	private byte secondaryExpectedPacket = 0;
	private DSScreen activeScreen = DSScreen.TOP;
	private ColorMode colorMode;
	
	private static final Logger logger = Logger.INSTANCE;
	
	/**
	 * Create an NTRUDPThread.
	 * @throws SocketException
	 */
	NTRUDPThread(DSScreen screen, ColorMode colorMode) throws SocketException {
		activeScreen = screen;
		socket = new DatagramSocket(8001);
		this.colorMode = colorMode;
	}
	
	public Frame getFrame() throws InterruptedException {
		return frameBuffer.take();
	}
	
	public void close() {
		shouldDie.set(true);
		socket.close();
	}

	@Override
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
					
					byte[] newData = new byte[priorityImageData.length+data.length-4];
					System.arraycopy(priorityImageData, 0, newData, 0, priorityImageData.length);
					System.arraycopy(data, 4, newData, priorityImageData.length, data.length-4);
					priorityImageData = newData;
					
					priorityExpectedPacket++;
					
					// Received a complete image, render
					if (isLastPacket) {
						
						priorityImage = ImageIO.read(new ByteArrayInputStream(priorityImageData));
						priorityImageData = new byte[0];
						
						priorityImage = ImageManipulator.adjust(priorityImage, colorMode, false);
						
						frameBuffer.add(new Frame(currentScreen, priorityImage));
						priorityImage = null;
						priorityExpectedFrame = 0;
	                    priorityExpectedPacket = 0;
					}
				} else if (currentScreen == activeScreen) {
					// Unexpected priority packet or frame
					priorityImageData = new byte[0];
					priorityImage = null;
					priorityExpectedFrame = 0;
                    priorityExpectedPacket = 0;
				} else if(secondaryExpectedPacket == currentPacket) {
					// Secondary screen
					
					byte[] newData = new byte[secondaryImageData.length+data.length-4];
					System.arraycopy(secondaryImageData, 0, newData, 0, secondaryImageData.length);
					System.arraycopy(data, 4, newData, secondaryImageData.length, data.length-4);
					secondaryImageData = newData;
					
					secondaryExpectedPacket++;
					
					// Received a complete image, render
					if (isLastPacket) {
						
						secondaryImage = ImageIO.read(new ByteArrayInputStream(secondaryImageData));
						secondaryImageData = new byte[0];
						
						secondaryImage = ImageManipulator.adjust(secondaryImage, colorMode, false);
						
						frameBuffer.add(new Frame(currentScreen, secondaryImage));
						secondaryImage = null;
						secondaryExpectedFrame = 0;
	                    secondaryExpectedPacket = 0;
					}
				} else {
					// Unexpected secondary packet or frame
					secondaryImageData = new byte[0];
					secondaryImage = null;
					secondaryExpectedFrame = 0;
                    secondaryExpectedPacket = 0;
				}
			} catch (IOException e) {
				close();
				e.printStackTrace();
			}
		}
	}
	
}
