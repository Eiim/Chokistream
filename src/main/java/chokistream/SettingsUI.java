package chokistream;

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
		Logger.INSTANCE.log(e.getClass().getSimpleName()+": "+e.getMessage());
	}
}
