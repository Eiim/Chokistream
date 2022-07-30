/**
 * 
 */

package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.scene.image.Image;

public class HZModClient implements StreamingInterface {
	
	private Socket client = null;
	private InputStream in = null;
	private OutputStream out = null;

	/**
	 * Create an HZModClient.
	 * @param host The host or IP to connect to.
	 * @param quality The quality to stream at.
	 * @param capCPU Cap CPU cycles.
	 */
	public HZModClient(String host, int quality, int capCPU) throws UnknownHostException, IOException {
		// Connect to TCP port and set up client
		client = new Socket(host, 8000);
		client.setTcpNoDelay(true);
		in = client.getInputStream();
		out = client.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public Image getFrame() {
		// TODO Auto-generated method stub
		return null;
	}

}
