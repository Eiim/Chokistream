package chokistream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;

public class ImageSequenceVideo implements VideoOutputInterface {
	
	private final StreamingInterface client;
	private final NetworkThread networkThread;
	private final InterpolationMode intrp;
	private final Layout layout;
	private final double topScale;
	private final double bottomScale;
	private final String directory;
	private final String prefix;
	private BufferedImage lastTopFrame;
	private BufferedImage lastBottomFrame;
	private boolean done;
	private int frameCount; // Used as topFrameCount for separate
	private int bottomFrameCount;
	
	private static final Logger logger = Logger.INSTANCE;
	
	public ImageSequenceVideo(StreamingInterface client, String directory, String prefix, InterpolationMode intrp, Layout layout, double topScale, double bottomScale) {
		this.client = client;
		this.directory = directory;
		this.prefix = prefix;
		this.intrp = intrp;
		this.layout = layout;
		this.topScale = topScale;
		this.bottomScale = bottomScale;
		lastTopFrame = new BufferedImage((int)(400*topScale), (int)(240*topScale), BufferedImage.TYPE_INT_RGB);
		lastBottomFrame = new BufferedImage((int)(320*bottomScale), (int)(240*bottomScale), BufferedImage.TYPE_INT_RGB);
		
		(new File(directory)).mkdirs();
		
		frameCount = 0;
		bottomFrameCount = 0;
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
		if(done) return;
		
		if(layout == Layout.TOP_ONLY && frame.screen == DSScreen.BOTTOM) return;
		if(layout == Layout.BOTTOM_ONLY && frame.screen == DSScreen.TOP) return;
		
		BufferedImage outIm = null;
		File outF = null;
		String formattedFrame = null;
		
		if(layout == Layout.SEPARATE) {
			switch(frame.screen) {
				case TOP:
					frameCount++;
					formattedFrame = String.format("%04d", frameCount);
					outF = new File(directory + File.separator + prefix + formattedFrame + "_top.png");
					outIm = Interpolator.scale(frame.image, intrp, topScale);
					break;
				case BOTTOM:
					bottomFrameCount++;
					formattedFrame = String.format("%04d", bottomFrameCount);
					outF = new File(directory + File.separator + prefix + formattedFrame + "_bototm.png");
					outIm = Interpolator.scale(frame.image, intrp, bottomScale);
					break;
			}
		} else {
			frameCount++;
			formattedFrame = String.format("%04d", frameCount);
			outF = new File(directory + File.separator + prefix + formattedFrame + ".png");
			if(frame.screen == DSScreen.TOP) lastTopFrame = Interpolator.scale(frame.image, intrp, topScale);
			else lastBottomFrame = Interpolator.scale(frame.image, intrp, bottomScale);
			
			outIm = switch(layout) {
				case TOP_ONLY -> lastTopFrame;
				case BOTTOM_ONLY -> lastBottomFrame;
				case HORIZONTAL -> ImageManipulator.combineHoriz(lastTopFrame, lastBottomFrame);
				case HORIZONTAL_INV -> ImageManipulator.combineHoriz(lastBottomFrame, lastTopFrame);
				case VERTICAL -> ImageManipulator.combineVert(lastTopFrame, lastBottomFrame);
				case VERTICAL_INV -> ImageManipulator.combineVert(lastBottomFrame, lastTopFrame);
				default -> throw new IllegalArgumentException("Unexpected value: " + layout); // Should never happen
			};
		}
		
		try {
			ImageIO.write(outIm, "PNG", outF);
			logger.log("Wrote "+outF.getCanonicalPath());
		} catch (IOException e) {
			displayError(e);
		}
	}

	@Override
	public void displayError(Exception e) {
		Writer buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		e.printStackTrace(pw);
		logger.log(buffer.toString());
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
}
