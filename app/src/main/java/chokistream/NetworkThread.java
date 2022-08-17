package chokistream;

import java.io.IOException;

public class NetworkThread extends Thread {
	
	private VideoOutputInterface output;
	private StreamingInterface input;
	private boolean active = true;
	
	/**
	 * Start processing frames indefinitely
	 */
	public void run() {
		while(active) {
			try {
				Frame f = input.getFrame();
				// This isn't perfect, but it should drastically reduce the frequency of attempting to encode extra frames
				if(active) {
					output.renderFrame(f);
				}
			} catch(InterruptedException e) {
				output.displayError(e);
			} catch(IOException e) {
				output.displayError(e);
			}
		}
	}
	
	public NetworkThread(StreamingInterface input, VideoOutputInterface output) {
		this.input = input;
		this.output = output;
	}
	
	/**
	 * @param out the output object (e.g. a JavaFXVideo)
	 */
	public void setOutput(VideoOutputInterface out) {
		output = out;
	}
	
	/**
	 * @param in the input object (e.g. a NTRClient)
	 */
	public void setInput(StreamingInterface in) {
		input = in;
	}
	
	public void stopRunning() {
		active = false;
	}
}
