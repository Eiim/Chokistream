package chokistream;

public enum LogMode implements EnumProp {
	CONSOLE("Console"),
	FILE("File"),
	BOTH("Both");
	
	private final String longName;
	
	private LogMode(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}