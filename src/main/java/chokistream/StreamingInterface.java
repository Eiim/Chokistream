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
	 * Gets the number of frames received since the last call.
	 * Implementations can choose how to count frames currently being processed. As such, this method shouldn't be relied upon for precision.
	 * @param screens Which screen to get the frame count for. If TOP or BOTTOM, get just frames for that screen. If BOTH, get combined frames.
	 * @return The number of frames.
	 */
	public int framesSinceLast(DSScreenBoth screens);
}
