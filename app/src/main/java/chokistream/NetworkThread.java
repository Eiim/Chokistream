package chokistream;

public class NetworkThread extends Thread {
	
	private VideoOutputInterface output;
	private StreamingInterface input;
	
	public void run() {
		while(true) {
			try {
				output.renderFrame(input.getFrame());
			} catch(InterruptedException e) {
				output.displayError(e);
			}
		}
	}
	
	public void setOutput(VideoOutputInterface out) {
		output = out;
	}
	
	public void setInput(StreamingInterface in) {
		input = in;
	}
}
