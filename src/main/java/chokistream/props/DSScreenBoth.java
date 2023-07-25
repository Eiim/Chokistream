package chokistream.props;

public enum DSScreenBoth implements EnumProp {
	TOP("Top"),
	BOTTOM("Bottom"),
	BOTH("Both");
	
	private final String longName;
	
	private DSScreenBoth(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
