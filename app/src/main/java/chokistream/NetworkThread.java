package chokistream;

public class NetworkThread extends Thread {
	
	private VideoOutputInterface output;
	private StreamingInterface input;
	
	public void run() {
		output.renderFrame(input.getFrame());
	}
	
	public void setOutput(VideoOutputInterface out) {
		output = out;
	}
	
	public void setInput(StreamingInterface in) {
		input = in;
	}
}
