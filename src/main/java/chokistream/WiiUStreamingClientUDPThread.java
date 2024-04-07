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

public class WiiUStreamingClientUDPThread extends Thread {
	
	/**
	 * Socket used to receive UDP packets from the 3DS on.
	 */
	private final DatagramSocket socket;
	
	/**
	 * Buffer used to store UDP data in.
	 */
	private final byte[] packetBuffer = new byte[1400];
	
	/**
	 * A BlockingQueue to buffer received Frames in.
	 */
	private final BlockingQueue<Frame> frameBuffer = new LinkedBlockingQueue<Frame>(10);
	
	/**
	 * Should the thread die?
	 */
	private final AtomicBoolean shouldDie = new AtomicBoolean(false);
	
	//WritableInputStream priorityInputStream = new WritableInputStream();
	//WritableInputStream secondaryInputStream = new WritableInputStream();
	byte[] priorityImageData = new byte[0];
	private int imageDataTotalLength = 0;
	
	byte[] secondaryImageData = new byte[0];
	private BufferedImage priorityImage;
	private BufferedImage secondaryImage;
	private byte priorityExpectedFrame = 0;
	private byte secondaryExpectedFrame = 0;
	private byte priorityExpectedPacket = 0;
	private byte secondaryExpectedPacket = 0;
	private final ColorMode colorMode;
	
	private static final Logger logger = Logger.INSTANCE;
	
	private int state = 0;
	
	WiiUStreamingClientUDPThread(ColorMode colorMode) throws SocketException {
		socket = new DatagramSocket(9445); // UDP port = 9445
		this.colorMode = colorMode;
	}
	
	public Frame getFrame() throws InterruptedException {
		return frameBuffer.take();
	}
	
	public void close() {
		shouldDie.set(true);
	}

	@Override
	public void run() {
		while (!shouldDie.get()) {
			DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
			try {
				socket.receive(packet);
				byte[] data = packet.getData();
				int length = packet.getLength();
				
				switch(state) {
				
				case 2: // Expected packet: JPEG data
					byte[] newData = new byte[priorityImageData.length+data.length];
					System.arraycopy(priorityImageData, 0, newData, 0, priorityImageData.length);
					System.arraycopy(data, 0, newData, priorityImageData.length, data.length);
					priorityImageData = newData;

					/* TODO
					 * if image data exceeds expected length, bail before the following 'if' statement.
					 * (but make this feature optional)
					 * i haven't done this yet because something is broken :P
					 */
					if (priorityImageData.length >= imageDataTotalLength) {
						// Received a complete image, render

						if (priorityImageData.length != imageDataTotalLength) {
							logger.log("Warning: Image data does not match expected length! "+priorityImageData.length+" != "+imageDataTotalLength);
						}

						try {
							priorityImage = ImageIO.read(new ByteArrayInputStream(priorityImageData));
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						//priorityImageData = new byte[0];
						//imageDataTotalLength = 0;
						
						// Bail if the JPEG is malformed.
						// (For a malformed JPEG, ImageIO.read doesn't throw an exception, it just returns null.)
						if (!(priorityImage == null)) {
							if (colorMode != ColorMode.REGULAR) {
								priorityImage = ImageManipulator.adjust(priorityImage, colorMode);
							}
							frameBuffer.poll();
							frameBuffer.add(new Frame(priorityImage));
						}
						priorityImageData = new byte[0];
						priorityImage = null;
	                    //imageDataTotalLength = 0;
	                    state = 0;
					}
					break;
					
				//case 0:
				//case 1:
				default:
					if (length == 4) {
						// TODO
					} else if (length == 8) { // Length of JPEG data
						// big-endian, 32-bit int
						//logger.log("received report of image data length.");
						//logger.log(data, LogLevel.REGULAR);
						imageDataTotalLength = data[4]<<24 & 0xff000000 | data[5]<<16 & 0xff0000 | data[6]<<8 & 0xff00 | data[7] & 0xff;
						priorityImageData = new byte[0];
						priorityImage = null;
						state = 2;
					}
					break;
				}
			} catch (IOException e) {
				close();
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
}
