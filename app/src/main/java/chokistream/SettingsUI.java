package chokistream;

import chokistream.props.EnumProp;
import chokistream.props.Prop;

public class SettingsUI {
	
	public int getPropInt(Prop<Integer> p) {
		return p.getDefault();
	}
	
	public String getPropString(Prop<String> p) {
		return p.getDefault();
	}
	
	public double getPropDouble(Prop<Double> p) {
		return p.getDefault();
	}
	
	/*
	 * Unfortunately we require a class instance here because of generic erasure. Subclasses use c
	 * for EnumProp.fromLogName. It'd be nice to find a way around this without making a separate
	 * method for each type, but I don't think that's possible.
	 */
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p, Class<T> c) {
		return p.getDefault();
	}
	
	// Sub-classes may or may not want to override
	public void displayError(Exception e) {
		Logger.INSTANCE.log(e.getClass().getSimpleName()+": "+e.getMessage());
	}
	
	public void saveSettings() {}
	
	public void loadSettings() {}
}
