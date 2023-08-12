package chokistream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import chokistream.props.DSScreen;

public class ImageSequenceVideo implements VideoOutputInterface {
	
	private StreamingInterface client;
	private NetworkThread networkThread;
	private static final Logger logger = Logger.INSTANCE;
	private boolean done;
	private int frameCount;
	private String directory;
	private String prefix;
	
	public ImageSequenceVideo(StreamingInterface client, String directory, String prefix) {
		this.client = client;
		this.directory = directory;
		this.prefix = prefix;
		
		(new File(directory)).mkdirs();
		
		frameCount = 0;
		networkThread = new NetworkThread(this.client, this);
		networkThread.start();
		
		// Add a runtime hook for when the process is terminated
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.log("Shutting down");
				done = true;
				kill();
			}
	    });
		
		logger.log("Starting file capture");
	}

	@Override
	public void renderFrame(Frame frame) {
		if(!done) {
			frameCount++;
			String postfix = frame.screen == DSScreen.TOP ? "_top" : "_bottom";
			String formattedFrame = String.format("%04d", frameCount);
			File out = new File(directory + File.separator + prefix + formattedFrame + postfix + ".png");
			try {
				ImageIO.write(orient(frame.image), "PNG", out);
				logger.log("Wrote "+out.getCanonicalPath());
			} catch (IOException e) {
				displayError(e);
			}
		}
	}

	@Override
	public void displayError(Exception e) {
		logger.logOnce(e.getClass()+": "+e.getMessage()+System.lineSeparator()+Arrays.toString(e.getStackTrace()));
	}

	@Override
	public void kill() {
		try {
			// Stop processing frames
			networkThread.stopRunning();
			// Close connection to 3DS
			client.close();
		} catch (IOException e) {
			displayError(e);
		}	
	}
	
	private static BufferedImage orient(BufferedImage in) {
		int origWidth = in.getWidth();
		int origHeight = in.getHeight();
		BufferedImage out = new BufferedImage(origHeight, origWidth, in.getType());
		for(int i = 0; i < origWidth; i++) {
			for(int j = 0; j < origHeight; j++) {
				out.setRGB(j, origWidth-i-1, in.getRGB(i, j));
			}
		}
		return out;
	}
}
