package chokistream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javafx.scene.image.Image;

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
	private Image priorityImage;
	private Image secondaryImage;
	private byte priorityExpectedFrame = 0;
	private byte secondaryExpectedFrame = 0;
	private byte priorityExpectedPacket = 0;
	private byte secondaryExpectedPacket = 0;
	private DSScreen activeScreen = DSScreen.TOP;
	private ColorMode colorMode;
	
	/**
	 * Create an NTRUDPThread.
	 * @param host The host or IP to connect to.
	 * @throws SocketException
	 */
	NTRUDPThread(String host, DSScreen screen, ColorMode colorMode) throws SocketException {
		activeScreen = screen;
		socket = new DatagramSocket(8001);
		this.colorMode = colorMode;
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
						priorityImage = new Image(priorityInputStream.getInputStream());
						priorityInputStream = new WritableInputStream();
						if (colorMode != ColorMode.REGULAR) {
							priorityImage = ColorHotfix.doColorHotfix(priorityImage, colorMode, false);
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
						secondaryImage = new Image(secondaryInputStream.getInputStream());
						secondaryInputStream = new WritableInputStream();
						if (colorMode != ColorMode.REGULAR) {
							secondaryImage = ColorHotfix.doColorHotfix(secondaryImage, colorMode, false);
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
