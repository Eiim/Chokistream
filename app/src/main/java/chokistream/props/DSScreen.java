package chokistream.props;

public enum DSScreen implements EnumProp {
	TOP("Top"),
	BOTTOM("Bottom");
	
	private final String longName;
	
	private DSScreen(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
