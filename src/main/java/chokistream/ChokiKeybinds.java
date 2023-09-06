package chokistream;

import java.util.HashMap;
import java.util.Map;

import chokistream.props.Controls;

public class ChokiKeybinds {

	private Map<Controls, Integer> binds = new HashMap<>();
	
	public void set(Controls c, int code) {
		binds.put(c, code);
	}
	
	public int get(Controls c) {
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
