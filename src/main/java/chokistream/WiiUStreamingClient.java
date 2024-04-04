package chokistream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.LogLevel;

public class WiiUStreamingClient implements StreamingInterface {
	
	private final WiiUStreamingClientUDPThread thread;
	
	private static final Logger logger = Logger.INSTANCE;
	
	private int frames;
	
	private String host;
	private int port;
	private Socket tcpSoc;
	private OutputStream tcpOut;
	private InputStream tcpIn;

	public WiiUStreamingClient(String host, int port, ColorMode colorMode) throws UnknownHostException, IOException, InterruptedException {
		this.host = host;
		this.port = port;
		
		connect();
		
		doPing();
		
		thread = new WiiUStreamingClientUDPThread(colorMode);
		thread.start();
		
		//disconnect();
	}

	@Override
	public void close() throws IOException {
		tcpSoc.close();
		thread.interrupt();
		thread.close();
	}
	
	@Override
	public Frame getFrame() throws InterruptedException {
		Frame f = thread.getFrame();
		frames++;
		return f;
	}
	
	@Override
	public int framesSinceLast(DSScreenBoth screens) {
		int f = frames;
		frames = 0;
		return f;
	}
	
	private void connect() {
		try {
			tcpSoc = new Socket(host, port); // TCP port = 8092
			tcpOut = tcpSoc.getOutputStream();
			tcpIn = tcpSoc.getInputStream();
		} catch(Exception e) {
			e.printStackTrace(); // TODO: replace
		}
		return;
	}
	
	private void disconnect() {
		try {
			tcpIn.close();
			tcpOut.close();
			tcpSoc.close();
		} catch(Exception e) {
			e.printStackTrace(); // TODO: replace
		}
		return;
	}
	
	public void doPing() {
		try {
			byte[] pingPak = new byte[] {0x15};
			tcpOut.write(pingPak);
			logger.log("Ping!");
			
			byte[] pongPak = new byte[1];
			tcpIn.read(pongPak);
			
			if(pongPak[0] == 0x16) {
				logger.log("Pong!");
			} else {
				logger.log("DEBUG: Invalid ping response.");
			}
		} catch(Exception e) {
			e.printStackTrace(); // TODO: replace
		}
		return;
	}
}
