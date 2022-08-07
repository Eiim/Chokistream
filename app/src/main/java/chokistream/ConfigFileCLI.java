package chokistream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import chokistream.INIParser.IniParseException;

public class ConfigFileCLI extends SettingsUI {
	INIParser parser;
	
	public ConfigFileCLI() {
		File f = new File("chokistream.ini");
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.out.println("Failed to create chokistream.ini");
			}
		}
		try {
			parser = new INIParser(f);
		} catch (FileNotFoundException | IniParseException e) {
			System.out.println("Failed to set up INI parser");
		}
	}
	
	public String getIp() throws InvalidOptionException {
		String ip = parser.getProperty("ip");
		if(ip.length() > 0) {
			return ip;
		} else {
			return this.ip;
		}
	}
	
	public int getQos() throws InvalidOptionException {
		String qos = parser.getProperty("qos");
		if(qos.length() > 0) {
			return Integer.parseInt(qos);
		} else {
			return this.qos;
		}
	}
	
	public int getQuality() throws InvalidOptionException {
		String qual = parser.getProperty("quality");
		if(qual.length() > 0) {
			return Integer.parseInt(qual);
		} else {
			return this.quality;
		}
	}
	
	public int getPriority() throws InvalidOptionException {
		String pri = parser.getProperty("priority");
		if(pri.length() > 0) {
			return Integer.parseInt(pri);
		} else {
			return this.priority;
		}
	}
	
	public NTRScreen getScreen() throws InvalidOptionException {
		String scr = parser.getProperty("priorityScreen");
		switch(scr) {
			case "":
				return this.screen;
			case "Top":
				return NTRScreen.TOP;
			case "Bottom":
				return NTRScreen.BOTTOM;
			default:
				throw new InvalidOptionException("priorityScreen", scr);
		}
	}
	
	public Mod getMod() throws InvalidOptionException {
		String mod = parser.getProperty("mod");
		switch(mod) {
			case "":
				return this.mod;
			case "HzMod":
				return Mod.HZMOD;
			case "NTR":
				return Mod.NTR;
			case "CHokiMod":
				return Mod.CHOKIMOD;
			default:
				throw new InvalidOptionException("mod", mod);
		}
	}
	
	public Layout getLayout() throws InvalidOptionException {
		String lay = parser.getProperty("layout");
		switch(lay) {
			case "":
				return this.layout;
			case "Separate":
				return Layout.SEPARATE;
			case "Vertical":
				return Layout.VERTICAL;
			case "Horizontal":
				return Layout.HORIZONTAL;
			case "Vertical (Inv)":
				return Layout.VERTICAL_INV;
			case "Horizontal (Inv)":
				return Layout.HORIZONTAL_INV;
			case "Top Only":
				return Layout.TOP_ONLY;
			case "Bottom Only":
				return Layout.BOTTOM_ONLY;
			default:
				throw new InvalidOptionException("layout", lay);
		}
	}
	
	public ColorMode getColorMode() throws InvalidOptionException {
		String cm = parser.getProperty("colorMode");
		switch(cm) {
			case "":
				return this.colorMode;
			case "Regular":
				return ColorMode.REGULAR;
			case "VC Blue Shift":
				return ColorMode.VC_BLUE_SHIFT;
			case "VC Blue Shift (Test)":
				return ColorMode.VC_BLUE_SHIFT;
			case "Grayscale":
				return ColorMode.GRAYSCALE;
			default:
				throw new InvalidOptionException("Color Mode", cm);
		}
	}
	
	public int getPort() throws InvalidOptionException {
		String port = parser.getProperty("property");
		if(port.length() > 0) {
			return Integer.parseInt(port);
		} else {
			return this.port;
		}
	}
	
	public double getTopScale() throws InvalidOptionException {
		String ts = parser.getProperty("topScale");
		if(ts.length() > 0) {
			return Double.parseDouble(ts);
		} else {
			return this.topScale;
		}
	}
	
	public double getBottomScale() throws InvalidOptionException {
		String bs = parser.getProperty("bottomScale");
		if(bs.length() > 0) {
			return Double.parseDouble(bs);
		} else {
			return this.bottomScale;
		}
	}
	
	public LogMode getLogMode() throws InvalidOptionException {
		String lm = parser.getProperty("logMode");
		switch(lm) {
			case "":
				return this.logMode;
			case "Console":
				return LogMode.CONSOLE;
			case "File":
				return LogMode.FILE;
			case "Both":
				return LogMode.BOTH;
			default:
				throw new InvalidOptionException("logMode", lm);
		}
	}
	
	public LogLevel getLogLevel() throws InvalidOptionException {
		String ll = parser.getProperty("logLevel");
		switch(ll) {
			case "":
				return this.logLevel;
			case "Regular":
				return LogLevel.REGULAR;
			case "Verbose":
				return LogLevel.VERBOSE;
			default:
				throw new InvalidOptionException("logLevel", ll);
		}
	}
	
	public String getLogFile() throws InvalidOptionException {
		String f = parser.getProperty("logFile");
		if(f.length() > 0) {
			return f;
		} else {
			return this.logFile;
		}
	}
	
	public InterpolationMode getIntrpMode() throws InvalidOptionException {
		String im = parser.getProperty("interpolationMode");
		switch(im) {
			case "":
				return this.intrp;
			case "None":
				return InterpolationMode.NONE;
			case "Smooth":
				return InterpolationMode.SMOOTH;
			default:
				throw new InvalidOptionException("interpolationMode", im);
		}
	}
	
	public int getDPI() throws InvalidOptionException {
		String dpi = parser.getProperty("dpi");
		if(dpi.length() > 0) {
			return Integer.parseInt(dpi);
		} else {
			return this.custDPI;
		}
	}
	
	public int getCapCPU() throws InvalidOptionException {
		String cap = parser.getProperty("cpuCap");
		if(cap.length() > 0) {
			return Integer.parseInt(cap);
		} else {
			return this.capCPU;
		}
	}
}