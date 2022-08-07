package chokistream;

public enum InterpolationMode implements EnumProp {
	SMOOTH("Smooth"),
	NONE("None");
	
	private final String longName;
	
	private InterpolationMode(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
