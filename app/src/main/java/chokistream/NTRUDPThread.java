package chokistream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage; // For hotfixColors method

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
    private NTRScreen activeScreen = NTRScreen.TOP;
	
	/**
	 * Create an NTRUDPThread.
	 * @param host The host or IP to connect to.
	 * @throws SocketException
	 */
	NTRUDPThread(String host, NTRScreen screen) throws SocketException {
		activeScreen = screen;
		socket = new DatagramSocket(8001);
	}
	
	public Frame getFrame() throws InterruptedException {
		return frameBuffer.take();
	}
	
	public void close() {
		socket.close();
	}
	
	/**
	 * Attempt to hot-fix the colors if the option is enabled(?)
	 * This is specifically aimed at Virtual Console games:
	 * Most notably Pokemon Yellow, but also Super Mario Bros, etc.
	 * 
	 * Theoretically can handle images with arbitrary dimensions and resolutions.
	 * If I implement it right, the Pixel Format of
	 * the hotfixImageInput shouldn't matter.
	 * 
	 * Assumes the input image's dimensions are each not 0 (or negative or null).
	 */
	private Image hotfixColors(Image hotfixImageInput) {
		WritableImage hotfixImageWritable = new WritableImage( (int)hotfixImageInput.getWidth() , (int)hotfixImageInput.getHeight() );
		Image hotfixImageOutput = null;
		int currentPixelArgbBefore = 0;
		int currentPixelArgbAfter = 0;
		int currentW = 0; // Current Width (position of pixel being processed)
		int currentH = 0; // Current Height
		/** 
		 * Pixel processing and transferring loops. Note they are zero-indexed.
		 * Also note. JFX has functions to automatically transfer pieces of the image,
		 * But we can't use that, because we need to play with the bit order ),:
		 */
		while (currentH < hotfixImageInput.getHeight()) {
			while (currentW < hotfixImageInput.getWidth()) {
/**
 * Logic for hot-fixing image colors! I am the king of hexadecimal. -C
 * 
 * Guide to what's what... (What this code intends to do.)
 * Letters ARGB stand for Alpha,Red,Green,Blue respectively.
 * Each group of eight letters is a byte. Each letter is a Bit.
 * The letter indicates which of the four the Bit contributes value to.
 * 
 * ...Did I explain that badly? Was that overly confusing? Oh well... -C
 *
 * What we receive (ARGB):AAAAAAAA RRRRRRRR GGGGGGGG  B  B  B  B B  BBB
 *                                 |||||||| ||||||||  |  |  |  | ?
 * How we (INTEND TO)              |||||||| ||||||||  |_ |_ |_ |_
 * shift bits:                     |||||||| ||||||||    |  |  |  |
 *                                 VVVVVVVV VVVVVVVV  ? V  V  V  V
 * What we draw (ARGB):   11111111 RRRRRRRR GGGGGGGG  B B  B  B  B  BBB
 */
				currentPixelArgbBefore = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
				
				// Here's the magic. On second thought, this should definitely be optimized with bitwise-AND,OR,NOT,XOR,etc.
				int newBluPixel = 0;
//				newBluPixel = ((int)((currentPixelArgbBefore%256)/16))*8 + (int)((currentPixelArgbBefore%256)/32);
//				newBluPixel = newBluPixel + ((int)(currentPixelArgbBefore/8)%2)*128;
				
				newBluPixel = currentPixelArgbBefore%256;
				newBluPixel = (int)((newBluPixel/256.0) * 0x48) + newBluPixel;
				
				currentPixelArgbAfter = ((int)(currentPixelArgbBefore/256))*256 + (newBluPixel%256);
				
				hotfixImageWritable.getPixelWriter().setArgb(currentW,currentH,currentPixelArgbAfter);
				currentW++;
			}
			currentW = 0;
			currentH++;
		}
		hotfixImageOutput = hotfixImageWritable;
		return hotfixImageOutput;
	}
	/**
	 * Main code block ^.^
	 */
	public void run() {
		while (!shouldDie.get()) {
			DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
			try {
				socket.receive(packet);
				byte[] data = packet.getData();
				byte currentFrame = data[0];
				NTRScreen currentScreen = ((data[1] & 0x0F) == 1) ? NTRScreen.TOP : NTRScreen.BOTTOM;
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
						if (1 == 1) { // Placeholder
							priorityImage = hotfixColors(priorityImage);
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
						if (1 == 1) { // Placeholder
							secondaryImage = hotfixColors(secondaryImage);
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
