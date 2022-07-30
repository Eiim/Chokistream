package chokistream;

public class HZModClient implements StreamingInterface {
	
	/**
	 * Internal buffer of frames.
	 */
	private byte[][] buffer;

	/**
	 * HzMod version:
	 * Connect to a streaming source.
	 * @param host The host or IP to connect to.
	 * @param port The port to connect to under the host.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 */
	public void connect(String host, int port, int quality, int capCPU) {
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
