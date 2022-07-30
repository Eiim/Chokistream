package chokistream;

import javafx.scene.image.Image;

public class VideoOutputInterface {
	
	protected StreamingInterface client;
	protected NetworkThread networkThread;
	
	public VideoOutputInterface(StreamingInterface client) {
		this.client = client;
		
		networkThread = new NetworkThread();
		networkThread.setInput(this.client);
		networkThread.setOutput(this);
		networkThread.start();
	}
	
	public void renderFrame(Frame frame) {}
	
	public void displayError(Exception e) {
		System.out.println(e.getClass().getSimpleName()+": "+e.getMessage());
	}
}
