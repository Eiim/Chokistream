package chokistream.props;

import java.awt.event.KeyEvent;

import chokistream.Input;

public enum Controls {

	SCREENSHOT("screenshot", "Screenshot", new Input(KeyEvent.VK_S)),
	CLOSE("close", "Close", new Input(KeyEvent.VK_BACK_SPACE)),
	QUALITY_UP("quality_up", "Increase Quality", new Input(KeyEvent.VK_UP)),
	QUALITY_DOWN("quality_down", "Decrease Quality", new Input(KeyEvent.VK_DOWN)),
	CPU_UP("cpu_up", "Increase CPU Cap", new Input(KeyEvent.VK_BRACERIGHT)),
	CPU_DOWN("cpu_down", "Decrease CPU Cap", new Input(KeyEvent.VK_BRACELEFT)),
	REQ_SCREEN("req_screen","Switch requested screen", new Input(KeyEvent.VK_R)),
	INTERLACE("interlace","Toggle interlacing", new Input(KeyEvent.VK_I)),
	TGA("tga","Toggle TGA", new Input(KeyEvent.VK_T));
	
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
