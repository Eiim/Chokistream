package chokistream.props;

public enum ColorMode implements EnumProp {
	REGULAR("Regular"),
	VC_BLUE_SHIFT("VC Blue Shift (Test)"),
	GRAYSCALE("Grayscale");
	
	private final String longName;
	
	private ColorMode(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
