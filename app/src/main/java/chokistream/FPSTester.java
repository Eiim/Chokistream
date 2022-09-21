package chokistream;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import chokistream.props.ColorMode;
import chokistream.props.DSScreenBoth;
import chokistream.props.InterpolationMode;
import chokistream.props.LogLevel;

public class FPSTester implements VideoOutputInterface {
	
	private int recF = 0;
	private int qual = 1;
	private Timer fpsTimer;
	private NetworkThread networkThread;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Logger.INSTANCE.setLevel(LogLevel.REGULAR);
		new FPSTester();
	}

	private FPSTester() throws UnknownHostException, IOException {
		
		//SettingsUI ui = new ConfigFileCLI();
		//StreamingInterface client = Main.initialize(ui);
		
		ChirunoModClient client = new ChirunoModClient("172.22.104.175", qual, false, false, false, 0, ColorMode.REGULAR,
				6464, DSScreenBoth.BOTTOM, 1.0, 1.0, InterpolationMode.NONE);
		
		networkThread = new NetworkThread(client, this);
		networkThread.start();
		
		fpsTimer = new Timer();
		fpsTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println(qual+","+(recF/10.0));
				recF = 0;
				if(qual < 100) {
					qual++;
					try {
						client.sendQuality(qual);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					networkThread.stopRunning();
				}
			}
		}, 10*1000, 10*1000);
	}
	
	@Override
	public void renderFrame(Frame frame) {
		//System.out.println("frame");
		recF++;
	}

	@Override
	public void displayError(Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub
		networkThread.stopRunning();
		fpsTimer.cancel();
	}
}
