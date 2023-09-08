package chokistream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		file.createNewFile();
		s = new Scanner(file, StandardCharsets.UTF_8);
		
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
			
			int eqpos = line.indexOf('=');
			if(eqpos == -1) {
				s.close();
				throw new IniParseException(line, lineNum);
			}
			String key = line.substring(0, eqpos).trim();
			String value = line.substring(eqpos+1).trim();
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
		
		Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
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
	
	private static class ParamData {
		int line;
		String value;
		
		ParamData(int l, String v) {
			line = l;
			value = v;
		}
	}
	
	public static class IniParseException extends Exception {
		private static final long serialVersionUID = -8122669746738785400L;
		String message;
		
		public IniParseException(String line, int lineNum) {
			message = "Failed to parse .ini line "+lineNum+":\n"+line;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
}
