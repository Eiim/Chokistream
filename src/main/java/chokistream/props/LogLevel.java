package chokistream.props;

public enum LogLevel implements EnumProp {
	REGULAR("Regular", 0),
	VERBOSE("Verbose", 10),
	EXTREME("Extreme", 20);
	
	private final String longName;
	private final double level;
	
	private LogLevel(String name, double level) {
		longName = name;
		this.level = level;
	}

	@Override
	public String getLongName() {
		return longName;
	}
	
	public double getLevelDouble() {
		return level;
	}
}