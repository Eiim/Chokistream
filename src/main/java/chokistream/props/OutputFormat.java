package chokistream.props;

public enum OutputFormat implements EnumProp {
	
	FILE("File Streaming"),
	VISUAL("Visual"),
	SEQUENCE("Image Sequence");
	
	private final String longName;
	
	private OutputFormat(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}

}
