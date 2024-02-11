package chokistream;

import java.util.HashMap;
import java.util.Map;

import chokistream.props.Controls;

public class ChokiKeybinds {

	private final Map<Controls, Input> binds = new HashMap<>();
	
	public void set(Controls c, Input code) {
		binds.put(c, code);
	}
	
	public Input get(Controls c) {
		return binds.get(c);
	}
	
	public static ChokiKeybinds getDefaults() {
		ChokiKeybinds ck = new ChokiKeybinds();
		for(Controls c : Controls.values()) {
			ck.set(c, c.getDefault());
		}
		return ck;
	}

}
