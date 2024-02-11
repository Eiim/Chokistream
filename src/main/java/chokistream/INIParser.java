package chokistream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chokistream.Input.InputParseException;
import chokistream.props.Controls;
import chokistream.props.EnumProp;
import chokistream.props.Prop;

// Very very simple INI handler
public class INIParser {
	
	private List<IniLine> iniLines;
	private HashMap<String, String> data;
	private final File file;
	
	public INIParser(File f) throws IOException {
		file = f;
		readFile();
	}
	
	public void readFile() throws IOException {
		iniLines = new ArrayList<>();
		data = new HashMap<>();
		file.createNewFile(); // only if it doesn't yet exist
		
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			
			// Ignore comments and sections
			if(line.length() == 0 || line.charAt(0) == ';' || line.charAt(0) == '[') {
				iniLines.add(new IniLine(false, line));
			} else {
				int eqpos = line.indexOf('=');
				if(eqpos == -1) {
					throw new IniParseException(line, i);
				}
				String key = line.substring(0, eqpos).trim();
				String value = line.substring(eqpos+1).trim();
				if(!data.containsKey(key)) {
					iniLines.add(new IniLine(true, key));
				}
				data.put(key, value);
			}
		}
	}
	
	public void writeFile() throws IOException {
		List<String> outLines = new ArrayList<>();
		for(IniLine l : iniLines) {
			if(l.isData) {
				outLines.add(l.key+" = "+data.get(l.key));
			} else {
				outLines.add(l.key);
			}
		}
		Files.write(file.toPath(), outLines, StandardCharsets.UTF_8);
	}
	
	public String getProperty(String prop) {
		if(data.containsKey(prop)) {
			return data.get(prop);
		} else {
			return "";
		}
	}
	
	public void setProperty(String prop, String value) throws IOException {
		if(!data.containsKey(prop))
			iniLines.add(new IniLine(true, prop)); // always add to end for now
		data.put(prop, value);
		
		writeFile();
	}
	
	public void setProp(Prop<?> prop, String value) throws IOException {
		setProperty(prop.getShortName(), value);
	}
	
	public void setProp(Prop<?> prop, EnumProp value) throws IOException {
		setProperty(prop.getShortName(), value.getLongName());
	}
	
	public void setProp(Prop<?> prop, int value) throws IOException {
		setProperty(prop.getShortName(), Integer.toString(value));
	}
	
	public void setProp(Prop<?> prop, double value) throws IOException {
		setProperty(prop.getShortName(), Double.toString(value));
	}
	
	public void setProp(Prop<?> prop, boolean value) throws IOException {
		setProperty(prop.getShortName(), Boolean.toString(value));
	}
	
	public void setProp(Controls c, Input i) throws IOException {
		setProperty("control_"+c.getShortName(), i.toString());
	}

	public void setProp(Controls c, String stringRep) throws IOException, InputParseException {
		setProperty("control_"+c.getShortName(), new Input(stringRep).toString());
	}

	public String getProp(Prop<?> prop) {
		return getProperty(prop.getShortName());
	}

	public String getProp(Controls c) {
		return getProperty("control_"+c.getShortName());
	}
	
	private static record IniLine(boolean isData, String key) {};
	
	public static class IniParseException extends IOException {
		private static final long serialVersionUID = -8122669746738785400L;
		String message;
		
		public IniParseException(String line, int lineNum) {
			message = "Failed to parse .ini at line "+lineNum+":\n"+line;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
}
