package chokistream;

import org.jcodec.common.Codec;

/*
 * An enum for all the different user-specifiable properties we use.
 * Hopefully maintainable than previous, more intuitive solution, which duplicated a lot of code.
 */

public enum Prop {
	IP("ip", "3DS IP", int.class),
	MOD("mod", "Streaming Mod", Mod.class),
	QUALITY("quality", "Quality", int.class),
	PRIORITYSCREEN("priorityScreen", "Priority Screen", NTRScreen.class),
	PRIORITYFACTOR("priority", "Priority Factor", int.class),
	QOS("qos", "QoS", int.class),
	CPUCAP("cpuCap", "CPU Cap", int.class),
	LAYOUT("layout", "Layout", Layout.class),
	COLORMODE("colorMode", "Color Mode", ColorMode.class),
	PORT("port", "3DS Port", int.class),
	TOPSCALE("topScale", "Top Screen Scale", double.class),
	BOTTOMSCALE("bottomScale", "Bottom Screen Scale", double.class),
	LOGMODE("logMode", "Log Mode", LogMode.class),
	LOGLEVEL("logLevel", "Log Level", LogLevel.class),
	LOGFILE("logFile", "Log File", String.class),
	INTRPMODE("interpolationMode", "Interpolation Mode", InterpolationMode.class),
	DPI("dpi", "DPI", int.class),
	VIDEOCODEC("codec", "Video Codec", Codec.class),
	VIDEOFILE("videoFile", "Video File", String.class);
	
	private String shortName; // short, camel-case name used in .ini
	private String longName; // longer, human-friendly name used in GUI
	private Class<?> type;
	
	private Prop(String sname, String lname, Class<?> t) {
		shortName = sname;
		longName = lname;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public boolean isEnumProp() {
		return type.isAssignableFrom(EnumProp.class);
	}
}
