package chokistream;

import java.io.File;
import java.io.IOException;

import chokistream.INIParser.IniParseException;
import chokistream.props.EnumProp;
import chokistream.props.Prop;

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
		} catch (IOException | IniParseException e) {
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
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p, Class<T> c) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return EnumProp.fromLongName(c, val);
		} else {
			return p.getDefault();
		}
	}
}