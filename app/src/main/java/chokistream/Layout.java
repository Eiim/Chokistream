package chokistream;

public enum Layout implements EnumProp {
	SEPARATE("Separate"),
	VERTICAL("Vertical"),
	VERTICAL_INV("Vertical (Inv)"),
	HORIZONTAL("Horizontal"),
	HORIZONTAL_INV("Horizontal (Inv)"),
	TOP_ONLY("Top Only"),
	BOTTOM_ONLY("Bottom Only");
	
	private final String longName;
	
	private Layout(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
