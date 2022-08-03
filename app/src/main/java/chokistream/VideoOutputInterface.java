package chokistream;

/**
 * A class to extend to provide an output location for a video.
 * Intended to be very agnostic of format, so you could for example write to a file.
 */
public class VideoOutputInterface {
	
	protected StreamingInterface client;
	protected NetworkThread networkThread;
	
	/**
	 * Instantiates a connection to a client using a NetworkThread. Does not start the connection.
	 * Sub-classes may find this method useful.
	 * 
	 * @param client the client to connect to
	 */
	public VideoOutputInterface(StreamingInterface client) {
		this.client = client;
		
		networkThread = new NetworkThread();
		networkThread.setInput(this.client);
		networkThread.setOutput(this);
	}
	
	/**
	 * Renders a frame. Sub-classes should override.
	 * 
	 * @param frame
	 */
	public void renderFrame(Frame frame) {}
	
	/**
	 * Displays an error. Sub-classes may want to override.
	 * 
	 * @param e The exception to display.
	 */
	public void displayError(Exception e) {
		Logger.INSTANCE.log(e.getClass().getSimpleName()+": "+e.getMessage());
	}
}
