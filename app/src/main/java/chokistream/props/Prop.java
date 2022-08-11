package chokistream.props;

import java.awt.Toolkit;

/*
 * An enum for all the different user-specifiable properties we use.
 * Hopefully maintainable than previous, more intuitive solution, which duplicated a lot of code.
 */

public class Prop<T> {
	public static final Prop<String> IP = new Prop<>("ip", "3DS IP", "0.0.0.0");
	public static final Prop<Mod> MOD = new Prop<>("mod", "Streaming Mod", Mod.NTR);
	public static final Prop<Integer> QUALITY = new Prop<>("quality", "Quality", 70);
	public static final Prop<DSScreen> PRIORITYSCREEN = new Prop<>("priorityScreen", "Priority Screen", DSScreen.TOP);
	public static final Prop<Integer> PRIORITYFACTOR = new Prop<>("priority", "Priority Factor", 8);
	public static final Prop<Integer> QOS = new Prop<>("qos", "QoS", 26);
	public static final Prop<Integer> CPUCAP = new Prop<>("cpuCap", "CPU Cap", 0);
	public static final Prop<DSScreen> REQSCREEN = new Prop<>("requestedScreen", "Requested Screen", DSScreen.TOP);
	public static final Prop<Layout> LAYOUT = new Prop<>("layout", "Layout", Layout.SEPARATE);
	public static final Prop<ColorMode> COLORMODE = new Prop<>("colorMode", "Color Mode", ColorMode.REGULAR);
	public static final Prop<Integer> PORT = new Prop<>("port", "3DS Port", 8000);
	public static final Prop<Double> TOPSCALE = new Prop<>("topScale", "Top Scale", 1.0);
	public static final Prop<Double> BOTTOMSCALE = new Prop<>("bottomScale", "Bottom Scale", 1.0);
	public static final Prop<LogMode> LOGMODE = new Prop<>("logMode", "Log Mode", LogMode.CONSOLE);
	public static final Prop<LogLevel> LOGLEVEL = new Prop<>("logLevel", "Log Level", LogLevel.REGULAR);
	public static final Prop<String> LOGFILE = new Prop<>("logFile", "Log File", "chokistream.log");
	public static final Prop<InterpolationMode> INTRPMODE = new Prop<>("interpolationMode", "Interpolation Mode", InterpolationMode.NONE);
	public static final Prop<Integer> DPI = new Prop<>("dpi", "DPI", Toolkit.getDefaultToolkit().getScreenResolution());
	public static final Prop<OutputFormat> OUTPUTFORMAT = new Prop<>("outputFormat", "Output Format", OutputFormat.VISUAL);
	public static final Prop<VideoFormat> VIDEOCODEC = new Prop<>("codec", "Video Codec", VideoFormat.PRORES);
	public static final Prop<String> VIDEOFILE = new Prop<>("videoFile", "Video File", "out");

	private final String shortName; // short, camel-case name used in .ini
	private final String longName; // longer, human-friendly name used in GUI
	private final T defaultVal;
	
	private Prop(String sname, String lname, T def) {
		shortName = sname;
		longName = lname;
		defaultVal = def;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}
	
	public T getDefault() {
		return defaultVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Prop<?> other = (Prop<?>) obj;
		// If we have the same long and short names, we're referring to the same property. No need to check defaults.
		return shortName.equals(other.shortName) && longName.equals(other.longName);
	}
}
