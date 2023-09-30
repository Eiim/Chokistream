package chokistream.props;

import java.awt.event.KeyEvent;

import chokistream.Input;

public enum Controls {

	SCREENSHOT("screenshot", "Screenshot", new Input(KeyEvent.VK_S)),
	RETURN("return", "Return", new Input(KeyEvent.VK_BACK_SPACE)),
	QUALITY_UP("quality_up", "Increase Quality", new Input(KeyEvent.VK_UP)),
	QUALITY_DOWN("quality_down", "Decrease Quality", new Input(KeyEvent.VK_DOWN));
	
	private String shortName;
	private String longName;
	private Input defaultKey;
	
	private Controls(String sname, String lname, Input def) {
		shortName = sname;
		longName = lname;
		defaultKey = def;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}
	
	public Input getDefault() {
		return defaultKey;
	}

}
