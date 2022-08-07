package chokistream;

public enum Mod implements EnumProp {
	NTR("NTR"),
	HZMOD("HzMod"),
	CHOKIMOD("CHokiMod");
	
	private final String longName;
	
	private Mod(String name) {
		longName = name;
	}

	@Override
	public String getLongName() {
		return longName;
	}
}
