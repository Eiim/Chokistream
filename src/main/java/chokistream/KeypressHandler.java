package chokistream;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import chokistream.props.Controls;

public class KeypressHandler implements KeyListener {
	
	private SwingVideo output;
	private StreamingInterface client;
	private ChokiKeybinds ck;
	
	public KeypressHandler(SwingVideo sv, StreamingInterface si, ChokiKeybinds keybinds) {
		output = sv;
		client = si;
		ck = keybinds;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Generic commands
		if(ck.get(Controls.SCREENSHOT).matches(e)) {
			output.screenshot();
		} else if(ck.get(Controls.RETURN).matches(e)) {
			output.kill();
		}
		
		try {
			// Client-specific commands
			if(client instanceof HZModClient) {
				HZModClient c = (HZModClient) client;
				
				if(ck.get(Controls.QUALITY_UP).matches(e)) {
					c.increaseQuality(1);
				} else if(ck.get(Controls.QUALITY_DOWN).matches(e)) {
					c.decreaseQuality(1);
				}
			} else if(client instanceof ChirunoModClient) {
				ChirunoModClient c = (ChirunoModClient) client;
				
				if(ck.get(Controls.QUALITY_UP).matches(e)) {
					c.increaseQuality(1);
				} else if(ck.get(Controls.QUALITY_DOWN).matches(e)) {
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
