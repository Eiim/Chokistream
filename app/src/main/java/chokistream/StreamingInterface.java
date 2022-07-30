/**
 * Defines an interface that all streaming clients must implement.
 */

package chokistream;

public interface StreamingInterface {
	
	/**
	 * Disconnect from the streaming source and tear down this object.
	 */
	public void close();
	
	/**
	 * Gets a single frame from the streaming source.
	 * @return The frame data.
	 */
	public byte[] getFrame();
	
}
