package chokistream.props;

/*
 * An enum for all the different user-specifiable properties we use.
 * Hopefully maintainable than previous, more intuitive solution, which duplicated a lot of code.
 */

public final class Prop<T> {
	private static int propIndex = 0;
	
	public static final Prop<String> IP = new Prop<>("ip", "3DS IP", "0.0.0.0", String.class, "IP of the 3DS");
	public static final Prop<Mod> MOD = new Prop<>("mod", "Streaming Mod", Mod.NTR, Mod.class, "3DS mod to connect to");
	public static final Prop<Integer> QUALITY = new Prop<>("quality", "Quality", 70, Integer.class, "");
	public static final Prop<Boolean> REQTGA = new Prop<>("tga", "Request TGA?", false, Boolean.class, "Request TARGA (lossless) image format.");
	public static final Prop<Boolean> INTERLACE = new Prop<>("interlace", "Interlace?", false, Boolean.class, "Request image interlacing for higher apparent FPS.");
	public static final Prop<DSScreen> PRIORITYSCREEN = new Prop<>("priorityScreen", "Priority Screen", DSScreen.TOP, DSScreen.class, "Prioritized screen");
	public static final Prop<Integer> PRIORITYFACTOR = new Prop<>("priority", "Priority Factor", 8, Integer.class, "Relative prioritization of prioritized screen");
	public static final Prop<Integer> QOS = new Prop<>("qos", "QoS", 26, Integer.class, "Packet QoS value (Set to >100 to disable)");
	public static final Prop<Integer> CPUCAP = new Prop<>("cpuCap", "CPU Cap", 0, Integer.class, "CPU usage limiter");
	public static final Prop<DSScreenBoth> REQSCREEN = new Prop<>("requestedScreen", "Requested Screen", DSScreenBoth.TOP, DSScreenBoth.class, "Requested 3DS screen");
	public static final Prop<Layout> LAYOUT = new Prop<>("layout", "Layout", Layout.SEPARATE, Layout.class, "Layout of the screens. Choose Top Only unless using a dual-screen mod/version.");
	public static final Prop<ColorMode> COLORMODE = new Prop<>("colorMode", "Color Mode", ColorMode.REGULAR, ColorMode.class, "Color correction options");
	public static final Prop<Integer> PORT = new Prop<>("port", "3DS Port", 8000, Integer.class, "3DS port, usually leave as default");
	public static final Prop<Double> TOPSCALE = new Prop<>("topScale", "Top Scale", 1.0, Double.class, "Factor to scale the top screen by");
	public static final Prop<Double> BOTTOMSCALE = new Prop<>("bottomScale", "Bottom Scale", 1.0, Double.class, "Factor to scale the bottom screen by");
	public static final Prop<LogMode> LOGMODE = new Prop<>("logMode", "Log Mode", LogMode.CONSOLE, LogMode.class, "Log to file or console");
	public static final Prop<LogLevel> LOGLEVEL = new Prop<>("logLevel", "Log Level", LogLevel.REGULAR, LogLevel.class, "Amount of detail in logs");
	public static final Prop<String> LOGFILE = new Prop<>("logFile", "Log File", "chokistream.log", String.class, "Filename for log file");
	public static final Prop<InterpolationMode> INTRPMODE = new Prop<>("interpolationMode", "Interpolation Mode", InterpolationMode.NONE, InterpolationMode.class, "");
	public static final Prop<OutputFormat> OUTPUTFORMAT = new Prop<>("outputFormat", "Output Format", OutputFormat.VISUAL, OutputFormat.class, "Output format. Switch to file streaming or image sequence for file output.");
	public static final Prop<VideoFormat> VIDEOCODEC = new Prop<>("codec", "Video Codec", VideoFormat.PRORES, VideoFormat.class, "Codec for video file output");
	public static final Prop<String> VIDEOFILE = new Prop<>("videoFile", "Video File", "out", String.class, "File name for video file output");
	public static final Prop<String> SEQUENCEDIR = new Prop<>("sequenceDir", "Image Directory", "out", String.class, "Directory for image sequences");
	public static final Prop<String> SEQUENCEPREFIX = new Prop<>("sequencePrefix", "Image Prefix", "", String.class, "Prefix for image sequence files");
	
	private final String shortName; // short, camel-case name used in .ini
	private final String longName; // longer, human-friendly name used in GUI
	private final T defaultVal;
	private final Class<T> c; // Helper lass instance
	private final int index;
	private final String tooltip;
	
	private Prop(String sname, String lname, T def, Class<T> c, String tt) {
		shortName = sname;
		longName = lname;
		defaultVal = def;
		this.c = c;
		tooltip = tt;
		index = propIndex++;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}
	
	public String getTooltip() {
		return tooltip;
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
		// If we have the same names, we're referring to the same property. No need to check anything else.
		return shortName.equals(other.shortName);
	}

	public int getIndex() {
		return index;
	}
	
	public static int getCount() {
		return propIndex;
	}
}
