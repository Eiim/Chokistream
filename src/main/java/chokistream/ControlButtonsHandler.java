package chokistream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JToggleButton;

import chokistream.props.Controls;

public class ControlButtonsHandler implements KeyListener {
	
	private final ArrayList<JToggleButton> buttons = new ArrayList<>(Controls.values().length);
	private JToggleButton active = null;
	private final JFrame frame;
	
	public ControlButtonsHandler(JFrame f) {
		frame = f;
	}
	
	public void add(JToggleButton button) {
		buttons.add(button);
		button.addActionListener(new ClickAction(button));
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(active != null && e.getKeyCode() != KeyEvent.VK_SHIFT && e.getKeyCode() != KeyEvent.VK_ALT && e.getKeyCode() != KeyEvent.VK_CONTROL) {
			active.setText(new Input(e).toString());
			active.setSelected(false);
			active = null;
			frame.pack();
		}
	}
	
	// non-static inner class! need to access `active` and `panel`
	private class ClickAction implements ActionListener {
		private final JToggleButton button;
		
		public ClickAction(JToggleButton button) {
			this.button = button;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(active != null) {
				active.setSelected(false);
			}
			active = button;
			frame.requestFocus();
		}
	}

	// Ignore
	@Override
	public void keyTyped(KeyEvent e) {}

	// Ignore
	@Override
	public void keyReleased(KeyEvent e) {}
}
