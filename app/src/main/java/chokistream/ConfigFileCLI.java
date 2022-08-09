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
	
	public int getPropInt(Prop<Integer> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return Integer.parseInt(val);
		} else {
			return p.getDefault();
		}
	}
	
	public String getPropString(Prop<String> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return val;
		} else {
			return p.getDefault();
		}
	}
	
	public double getPropDouble(Prop<Double> p) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return Double.parseDouble(val);
		} else {
			return p.getDefault();
		}
	}
	
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p, Class<T> c) {
		String val = parser.getProperty(p.getShortName());
		if(val.length() > 0) {
			return EnumProp.fromLongName(c, val);
		} else {
			return p.getDefault();
		}
	}
}