package chokistream;

import java.io.IOException;

/**
 * Defines an interface that all streaming clients must implement.
 */
public interface StreamingInterface {
	
	/**
	 * Disconnect from the streaming source and tear down this object.
	 */
	public void close() throws IOException;
	
	/**
	 * Gets a single frame from the streaming source.
	 * @return The frame data.
	 */
	public Frame getFrame() throws InterruptedException;
	
}
