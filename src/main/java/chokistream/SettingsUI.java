package chokistream;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import chokistream.props.EnumProp;
import chokistream.props.Prop;

public abstract class SettingsUI {
	
	public int getPropInt(Prop<Integer> p) {
		return p.getDefault();
	}
	
	public String getPropString(Prop<String> p) {
		return p.getDefault();
	}
	
	public double getPropDouble(Prop<Double> p) {
		return p.getDefault();
	}
	
	public boolean getPropBoolean(Prop<Boolean> p) {
		return p.getDefault();
	}
	
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p) {
		return p.getDefault();
	}
	
	public ChokiKeybinds getKeybinds() {
		return ChokiKeybinds.getDefaults();
	}
	
	// Sub-classes may or may not want to override
	public void displayError(Exception e) {
		Writer buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		e.printStackTrace(pw);
		Logger.INSTANCE.log(buffer.toString());
	}
}
