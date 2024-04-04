package chokistream.props;

public enum Mod implements EnumProp {
	NTR("NTR"),
	HZMOD("HzMod"),
	CHIRUNOMOD("ChirunoMod"),
	WIIUSTREAMINGPLUGIN("Wii U Streaming Plugin");
	
	private final String longName;
	
	private Mod(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
