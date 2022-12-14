package chokistream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import chokistream.props.EnumProp;
import chokistream.props.Prop;

// Very very simple INI handler
public class INIParser {
	
	private HashMap<String, ParamData> params = new HashMap<>();
	private File file;
	private int totalLines;
	
	public INIParser(File f) throws IOException, IniParseException {
		file = f;
		Scanner s;
		try {
			s = new Scanner(file);
		} catch(FileNotFoundException e) {
			file.createNewFile();
			s = new Scanner(file);
		}
		int lineNum = 0;
		while(s.hasNextLine()) {
			String line = s.nextLine().trim();
			lineNum++;
			if(line.length() == 0) {
				break;
			}
			
			// Ignore comments and sections
			if(line.charAt(0) == ';' || line.charAt(0) == '[') {
				break;
			}
			
			String[] components = line.split("=");
			if(components.length != 2) {
				s.close();
				throw new IniParseException(line, lineNum);
			}
			String key = components[0].trim();
			String value = components[1].trim();
			if(value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
			params.put(key, new ParamData(lineNum, value));
		}
		s.close();
		totalLines = lineNum;
	}
	
	public String getProperty(String prop) {
		if(params.containsKey(prop)) {
			return params.get(prop).value;
		} else {
			return "";
		}
	}
	
	public void setProperty(String prop, String value) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		if(params.containsKey(prop)) {
			int lineNum = params.get(prop).line;
			lines.set(lineNum-1, prop+" = "+value);
			params.put(prop, new ParamData(totalLines, value));
		} else {
			lines.add(prop+" = "+value);
			totalLines++;
			params.put(prop, new ParamData(totalLines, value));
		}
		
		Files.write(file.toPath(), lines);
	}
	
	public void setProperty(String prop, int value) throws IOException {
		setProperty(prop, Integer.toString(value));
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
	
	public String getProp(Prop<?> prop) {
		return getProperty(prop.getShortName());
	}
	
	private class ParamData {
		int line;
		String value;
		
		ParamData(int l, String v) {
			line = l;
			value = v;
		}
	}
	
	public class IniParseException extends Exception {
		private static final long serialVersionUID = -8122669746738785400L;
		String message;
		
		public IniParseException(String line, int lineNum) {
			message = "Failed to parse .ini line "+lineNum+":\n"+line;
		}
		
		public String getMessage() {
			return message;
		}
	}
}
