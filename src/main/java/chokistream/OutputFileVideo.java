package chokistream;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.VideoFormat;

public class OutputFileVideo implements VideoOutputInterface {
	
	private StreamingInterface client;
	private NetworkThread networkThread;
	private SequenceEncoder enc;
	private long prevNanos;
	private static final Logger logger = Logger.INSTANCE;
	private boolean done;
	private InterpolationMode intrp;
	private double topScale;
	private double bottomScale;
	
	public OutputFileVideo(StreamingInterface client, Layout layout, String file, VideoFormat vf, InterpolationMode intrp, double topScale, double bottomScale) {
		this.client = client;
		this.topScale = topScale;
		this.bottomScale = bottomScale;
		// Maybe move this down?
		networkThread = new NetworkThread(this.client, this);
		
		try {
			enc = new SequenceEncoder(NIOUtils.writableChannel(new File(file)), 
					Rational.R1(60), vf.getFormat(), vf.getCodec(), null);
		} catch (IOException e) {
			displayError(e);
		}
		prevNanos = System.nanoTime();
		
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
	public void renderFrame(Frame f) {
		if(!done) {
			if(f.screen == DSScreen.TOP) {
				long newNanos = System.nanoTime();
				int frames = (int) (Math.round(newNanos-prevNanos)/16666667f);
				prevNanos += (frames * 16666667l); // Nanos of the frame boundary
				try {
					for(int i = 0; i < frames; i++) {
						enc.encodeNativeFrame(AWTUtil.fromBufferedImageRGB(Interpolator.scale(f.image, intrp, topScale)));
					}
				} catch (IOException e) {
					displayError(e);
				}
			}
		}
	}
	
	@Override
	public void kill() {
		try {
			// Stop processing frames
			networkThread.stopRunning();
			// Close connection to 3DS
			client.close();
			// Finish up video output
			enc.finish();
			done = true;
		} catch (IOException e) {
			displayError(e);
		}	
	}
	
	@Override
	public void displayError(Exception e) {
		logger.log(e.getClass()+": "+e.getMessage()+System.lineSeparator()+Arrays.toString(e.getStackTrace()));
	}
}
