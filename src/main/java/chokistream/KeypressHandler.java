package chokistream;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import chokistream.props.Controls;

public class KeypressHandler implements KeyListener {
	
	private SwingVideo output;
	private StreamingInterface client;
	private ChokiKeybinds ck;
	private static final Logger logger = Logger.INSTANCE;
	
	public KeypressHandler(SwingVideo sv, StreamingInterface si, ChokiKeybinds keybinds) {
		output = sv;
		client = si;
		ck = keybinds;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Generic commands
		if(e.getKeyCode() == ck.get(Controls.SCREENSHOT)) {
			output.screenshot();
		} else if(e.getKeyCode() == ck.get(Controls.RETURN)) {
			output.kill();
		}
		
		try {
			// Client-specific commands
			if(client instanceof HZModClient) {
				HZModClient c = (HZModClient) client;
				
				if(e.getKeyCode() == ck.get(Controls.QUALITY_UP)) {
					c.increaseQuality(1);
				} else if(e.getKeyCode() == ck.get(Controls.QUALITY_DOWN)) {
					c.decreaseQuality(1);
				}
			} else if(client instanceof ChirunoModClient) {
				ChirunoModClient c = (ChirunoModClient) client;
				
				if(e.getKeyCode() == ck.get(Controls.QUALITY_UP)) {
					c.increaseQuality(1);
				} else if(e.getKeyCode() == ck.get(Controls.QUALITY_DOWN)) {
					c.decreaseQuality(1);
				}
			}
		} catch(IOException e1) {
			output.displayError(e1);
		}
	}
	
	// These methods need to be overridden, but aren't useful.
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
}
