package chokistream;

public class NetworkThread extends Thread {
	
	private VideoOutputInterface output;
	private StreamingInterface input;
	
	/**
	 * Start processing frames indefinitely
	 */
	public void run() {
		while(true) {
			try {
				output.renderFrame(input.getFrame());
			} catch(InterruptedException e) {
				output.displayError(e);
			}
		}
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
}
