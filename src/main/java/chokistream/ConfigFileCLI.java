package chokistream;

import java.io.File;
import java.io.IOException;

import chokistream.props.EnumProp;
import chokistream.props.Prop;

public class ConfigFileCLI extends SettingsUI {
	INIParser parser;
	
	// Logger doesn't exist yet, so we have to use System.out.printlns instead.
	public ConfigFileCLI() {
		File f = new File("chokistream.ini");
		if(!f.exists()) {
			try {
				f.createNewFile();
				System.out.println("Created new chokistream.ini");
			} catch (IOException e) {
				System.out.println("Failed to create chokistream.ini");
			}
		}
		try {
			parser = new INIParser(f);
		} catch (IOException e) {
			System.out.println("Failed to set up INI parser");
		}
	}
	
	@Override
	public int getPropInt(Prop<Integer> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return Integer.parseInt(val);
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public String getPropString(Prop<String> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return val;
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public double getPropDouble(Prop<Double> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return Double.parseDouble(val);
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public boolean getPropBoolean(Prop<Boolean> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return Boolean.parseBoolean(val);
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return EnumProp.fromLongName(p.propClass(), val);
		} else {
			return p.getDefault();
		}
	}
}