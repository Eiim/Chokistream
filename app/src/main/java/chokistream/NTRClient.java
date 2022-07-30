/**
 * 
 */

package chokistream;

public class NTRClient implements StreamingInterface {
	
	/**
	 * Internal buffer of frames.
	 */
	private byte[][] buffer;

	/**
	 * NTR version:
	 * Connect to a streaming source.
	 * @param host The host or IP to connect to.
	 * @param port The port to connect to under the host.
	 * @param quality The quality to stream at.
	 * @param screen Which screen gets priority.
	 * @param priority Priority factor.
	 * @param qos QoS value.
	 */
	public void connect(String host, int port, int quality, boolean screen, int priority, int qos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getFrame() {
		// TODO Auto-generated method stub
		return null;
	}

}
