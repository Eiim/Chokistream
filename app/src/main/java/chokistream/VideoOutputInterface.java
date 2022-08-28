package chokistream;

/**
 * A class to extend to provide an output location for a video.
 * Intended to be very agnostic of format, so you could for example write to a file.
 */
public interface VideoOutputInterface {
	
	/**
	 * Renders a frame.
	 * 
	 * @param frame
	 */
	public void renderFrame(Frame frame);
	
	/**
	 * Displays an error.
	 * 
	 * @param e The exception to display.
	 */
	public void displayError(Exception e);
	
	/**
	 * Kills the output.
	 */
	public void kill();
}
