package chokistream;

import java.io.IOException;

import chokistream.props.DSScreenBoth;

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
	public Frame getFrame() throws InterruptedException, IOException;
	
	/**
	 * Gets the number of frames recieved since the last call.
	 * @return The number of frames
	 */
	public int getFrameCount(DSScreenBoth screens);
}
