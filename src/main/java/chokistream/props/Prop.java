package chokistream.props;

/*
 * An enum for all the different user-specifiable properties we use.
 * Hopefully maintainable than previous, more intuitive solution, which duplicated a lot of code.
 */

public class Prop<T> {
	public static final Prop<String> IP = new Prop<>("ip", "3DS IP", "0.0.0.0", String.class);
	public static final Prop<Mod> MOD = new Prop<>("mod", "Streaming Mod", Mod.NTR, Mod.class);
	public static final Prop<Integer> QUALITY = new Prop<>("quality", "Quality", 70, Integer.class);
	public static final Prop<Boolean> REQTGA = new Prop<>("tga", "Request TGA?", false, Boolean.class); // ChirunoMod only
	public static final Prop<Boolean> INTERLACE = new Prop<>("interlace", "Interlace?", false, Boolean.class);
	public static final Prop<DSScreen> PRIORITYSCREEN = new Prop<>("priorityScreen", "Priority Screen", DSScreen.TOP, DSScreen.class);
	public static final Prop<Integer> PRIORITYFACTOR = new Prop<>("priority", "Priority Factor", 8, Integer.class);
	public static final Prop<Integer> QOS = new Prop<>("qos", "QoS", 26, Integer.class);
	public static final Prop<Integer> CPUCAP = new Prop<>("cpuCap", "CPU Cap", 0, Integer.class);
	public static final Prop<DSScreenBoth> REQSCREEN = new Prop<>("requestedScreen", "Requested Screen", DSScreenBoth.TOP, DSScreenBoth.class);
	public static final Prop<Layout> LAYOUT = new Prop<>("layout", "Layout", Layout.SEPARATE, Layout.class);
	public static final Prop<ColorMode> COLORMODE = new Prop<>("colorMode", "Color Mode", ColorMode.REGULAR, ColorMode.class);
	public static final Prop<Integer> PORT = new Prop<>("port", "3DS Port", 8000, Integer.class);
	public static final Prop<Double> TOPSCALE = new Prop<>("topScale", "Top Scale", 1.0, Double.class);
	public static final Prop<Double> BOTTOMSCALE = new Prop<>("bottomScale", "Bottom Scale", 1.0, Double.class);
	public static final Prop<LogMode> LOGMODE = new Prop<>("logMode", "Log Mode", LogMode.CONSOLE, LogMode.class);
	public static final Prop<LogLevel> LOGLEVEL = new Prop<>("logLevel", "Log Level", LogLevel.REGULAR, LogLevel.class);
	public static final Prop<String> LOGFILE = new Prop<>("logFile", "Log File", "chokistream.log", String.class);
	public static final Prop<InterpolationMode> INTRPMODE = new Prop<>("interpolationMode", "Interpolation Mode", InterpolationMode.NONE, InterpolationMode.class);
	public static final Prop<OutputFormat> OUTPUTFORMAT = new Prop<>("outputFormat", "Output Format", OutputFormat.VISUAL, OutputFormat.class);
	public static final Prop<VideoFormat> VIDEOCODEC = new Prop<>("codec", "Video Codec", VideoFormat.PRORES, VideoFormat.class);
	public static final Prop<String> VIDEOFILE = new Prop<>("videoFile", "Video File", "out", String.class);
	public static final Prop<String> SEQUENCEDIR = new Prop<>("sequenceDir", "Image Directory", "out", String.class);
	public static final Prop<String> SEQUENCEPREFIX = new Prop<>("sequencePrefix", "Image Prefix", "", String.class);
	
	private final String shortName; // short, camel-case name used in .ini
	private final String longName; // longer, human-friendly name used in GUI
	private final T defaultVal;
	private final Class<T> c; // Helper lass instance
	
	private Prop(String sname, String lname, T def, Class<T> c) {
		shortName = sname;
		longName = lname;
		defaultVal = def;
		this.c = c;
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
	
	public Class<T> propClass() {
		return c;
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
