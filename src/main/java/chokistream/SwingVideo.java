package chokistream;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;

public class SwingVideo implements VideoOutputInterface {
	
	private final StreamingInterface client;
	private final NetworkThread networkThread;
	private final ArrayList<JFrame> frames = new ArrayList<>();
	private final KeyListener kl;
	private ImageComponent topImageView;
	private ImageComponent bottomImageView;
	private Timer fpsTimer;
	
	private static final Logger logger = Logger.INSTANCE;

	public SwingVideo(StreamingInterface client, Layout layout, double topScale, double bottomScale, InterpolationMode intrp, ChokiKeybinds kb, boolean showFPS, JFrame top, JFrame bottom, JFrame both) {
		this.client = client;
		kl = new KeypressHandler(this, client, kb);
		
		networkThread = new NetworkThread(this.client, this);
		
		switch(layout) {
		case SEPARATE: {
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			top.add(topImageView);
			bottom.add(bottomImageView);
			frames.add(top);
			frames.add(bottom);
			break;
		} case TOP_ONLY: {
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			top.add(topImageView);
			frames.add(top);
			break;
		} case BOTTOM_ONLY: {
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			bottom.add(bottomImageView);
			frames.add(bottom);
			break;
		} case HORIZONTAL: {
			both.setLayout(new BoxLayout(both.getContentPane(), BoxLayout.X_AXIS));
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			topImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(topImageView);
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			bottomImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(bottomImageView);
			frames.add(both);
			break;
		} case HORIZONTAL_INV: {
			both.setLayout(new BoxLayout(both.getContentPane(), BoxLayout.X_AXIS));
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			bottomImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(bottomImageView);
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			topImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(topImageView);
			frames.add(both);
			break;
		} case VERTICAL: {
			both.setLayout(new BoxLayout(both.getContentPane(), BoxLayout.Y_AXIS));
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			topImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(topImageView);
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			bottomImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(bottomImageView);
			frames.add(both);
			break;
		} case VERTICAL_INV: {
			both.setLayout(new BoxLayout(both.getContentPane(), BoxLayout.Y_AXIS));
			bottomImageView = new ImageComponent(DSScreen.BOTTOM, bottomScale, intrp);
			bottomImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(bottomImageView);
			topImageView = new ImageComponent(DSScreen.TOP, topScale, intrp);
			topImageView.setAlignmentX(Component.CENTER_ALIGNMENT);
			both.add(topImageView);
			frames.add(both);
			break;
		} default: {
			logger.log("Unsupported layout!");
			return;
		}}
		
		for(JFrame f : frames) {
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setResizable(false);
			f.setIconImages(IconLoader.getAll());
			f.addWindowListener(new WindowAdapter() {
			    @Override
			    public void windowClosing(WindowEvent e) {
			    	kill();
			    }
			});
			f.addKeyListener(kl);
			f.getContentPane().setBackground(Color.BLACK);
			f.pack();
			f.setVisible(true);
			f.repaint(); // Fix Windows bug when re-using JFrames
		}
		
		if(showFPS) {
			fpsTimer = new Timer();
			fpsTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					int topFPS = client.framesSinceLast(DSScreenBoth.TOP);
					int bottomFPS = client.framesSinceLast(DSScreenBoth.BOTTOM);
					
					if(frames.size() == 2) {
						frames.get(0).setTitle("Chokistream - 3DS Top Screen ("+topFPS+" FPS)");
						frames.get(1).setTitle("Chokistream - 3DS Bottom Screen ("+bottomFPS+" FPS)");
					} else {
						frames.get(0).setTitle("Chokistream - 3DS ("+Math.max(topFPS, bottomFPS)+" FPS)");
					}
				}
			}, 1000, 1000);
		}
		
		networkThread.start();
		
		logger.log("Starting Swing video", LogLevel.VERBOSE);
	}
	
	public void screenshot() {
		try {
			// Only take the screenshots if the image has rendered - mostly for HzMod, but perhaps
			// also if the images just haven't come yet because we're still initializing
			if(topImageView.getImage() != null) {
				File ft = new File("chokistream_top.png");
				ImageIO.write(topImageView.getImage(), "png", ft);
			}
			if(bottomImageView.getImage() != null) {
				File fb = new File("chokistream_bottom.png");
				ImageIO.write(bottomImageView.getImage(), "png", fb);
			}
			logger.log("Took a screenshot!", LogLevel.VERBOSE);
		} catch (IOException e1) {
			displayError(e1);
		}
	}

	@Override
	public void renderFrame(Frame frame) {
		if(frame.screen == DSScreen.TOP) {
			if(topImageView != null)
				topImageView.updateImage(frame.image);
		} else {
			if(bottomImageView != null)
				bottomImageView.updateImage(frame.image);
		}
	}

	@Override
	public void displayError(Exception e) {
		Writer buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		e.printStackTrace(pw);
		logger.log(buffer.toString());
		JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void kill() {
		if(fpsTimer != null) fpsTimer.cancel();
		networkThread.stopRunning();
		try {
			client.close();
		} catch (IOException e) {
			displayError(e);
		}
		for(JFrame f : frames) {
			f.setVisible(false);
			f.removeKeyListener(kl);
			((JPanel)f.getContentPane()).removeAll(); // Remove previous image view
		}
	}
}
