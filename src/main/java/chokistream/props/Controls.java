package chokistream.props;

import java.awt.event.KeyEvent;

public enum Controls {

	SCREENSHOT("screenshot", "Screenshot", KeyEvent.VK_S),
	RETURN("return", "Return", KeyEvent.VK_BACK_SPACE),
	QUALITY_UP("quality_up", "Increase Quality", KeyEvent.VK_UP),
	QUALITY_DOWN("quality_down", "Decrease Quality", KeyEvent.VK_DOWN);
	
	private String shortName;
	private String longName;
	private int defaultKey;
	
	private Controls(String sname, String lname, int def) {
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
	
	public int getDefault() {
		return defaultKey;
	}

}
