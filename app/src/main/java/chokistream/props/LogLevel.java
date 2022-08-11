package chokistream.props;

public enum LogLevel implements EnumProp {
	REGULAR("Regular"),
	VERBOSE("Verbose");
	
	private final String longName;
	
	private LogLevel(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}