package chokistream;

public enum NTRScreen implements EnumProp {
	TOP("Top"),
	BOTTOM("Bottom");
	
	private final String longName;
	
	private NTRScreen(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
