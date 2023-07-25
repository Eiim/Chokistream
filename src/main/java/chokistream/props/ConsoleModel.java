package chokistream.props;

public enum ConsoleModel implements EnumProp {
	N3DS("New 3DS"),
	O3DS("Old 3DS");
	
	private final String longName;
	
	private ConsoleModel(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
