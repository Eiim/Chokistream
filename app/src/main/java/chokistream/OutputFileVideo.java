package chokistream;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import chokistream.props.DSScreen;
import chokistream.props.Layout;
import chokistream.props.VideoFormat;

public class OutputFileVideo extends VideoOutputInterface {
	
	private SequenceEncoder enc;
	private long startNanos;
	private long prevNanos;
	private static final Logger logger = Logger.INSTANCE;
	
	public OutputFileVideo(StreamingInterface client, Layout layout, String file, VideoFormat vf) {
		super(client);
		try {
			enc = new SequenceEncoder(NIOUtils.writableChannel(new File(file)), 
					Rational.R1(60), vf.getFormat(), vf.getCodec(), null);
		} catch (IOException e) {
			displayError(e);
		}
		startNanos = System.nanoTime();
		prevNanos = startNanos;
		
		networkThread.start();
		
		// Add a runtime hook for when the process is terminated
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.log("Shutting down");
				kill();
			}
	    });
		
		logger.log("Starting file capture");
	}
	
	public void renderFrame(Frame f) {
		if(f.screen == DSScreen.TOP) {
			long newNanos = System.nanoTime();
			int frames = (int) (Math.round(newNanos-prevNanos)/16666667f);
			//logger.log(""+frames);
			prevNanos += ((long)frames * 16666667l); // Nanos of the frame boundary
			try {
				enc.encodeNativeFrame(AWTUtil.fromBufferedImageRGB(f.image));
			} catch (IOException e) {
				displayError(e);
			}
		}
	}
	
	public void kill() {
		try {
			// Stop processing frames
			networkThread.stopRunning();
			// Finish up video output
			enc.finish();
			// Close connection to 3DS
			client.close();
		} catch (IOException e) {
			displayError(e);
		}	
	}
	
	public void displayError(Exception e) {
		logger.logOnce(e.getClass()+"\n"+Arrays.toString(e.getStackTrace()));
	}
}
