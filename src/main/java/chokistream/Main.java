package chokistream;

import java.util.Arrays;
import java.util.List;

import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;
import chokistream.props.Mod;
import chokistream.props.Prop;
import chokistream.props.VideoFormat;

/**
 * Determines the UI to use and initializes it
 */
public class Main {
	public static void main(String[] args) {
		
		// Set up logging before anything else
		SettingsUI ui = new ConfigFileCLI();
		LogLevel level = ui.getPropEnum(Prop.LOGLEVEL);
		LogMode mode = ui.getPropEnum(Prop.LOGMODE);
		String logFile = ui.getPropString(Prop.LOGFILE);
    	Logger.INSTANCE.init(mode, level, logFile);
    	
    	List<String> argsAL = Arrays.asList(args);
		if(argsAL.contains("--console") || argsAL.contains("-c")) {
			switch(ui.getPropEnum(Prop.OUTPUTFORMAT)) {
			case FILE:
				initializeFile(ui);
				break;
			case SEQUENCE:
				initializeSequence(ui);
				break;
			default:
				Logger.INSTANCE.log("Output format not supported from console!");
				break;
			}
		} else {
			new SwingGUI();
		}
	}
	
	private static StreamingInterface initialize(SettingsUI ui) {
		// These are universal, so get these first and then sort out the rest by mod.
		// Quality could be here except the logic is different in CHM.
    	Mod mod;
    	String ip;
    	int port;
    	double topScale;
    	double bottomScale;
    	InterpolationMode intrp;
    	try {
			mod = ui.getPropEnum(Prop.MOD);
			ip = ui.getPropString(Prop.IP);
			port = ui.getPropInt(Prop.PORT);
			topScale = ui.getPropDouble(Prop.TOPSCALE);
			bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			intrp = ui.getPropEnum(Prop.INTRPMODE);
		} catch (RuntimeException e) {
			ui.displayError(e);
			return null;
		}
    	
    	StreamingInterface client = null;
    	
    	switch(mod) {
    		case NTR:
				try {
					int quality = ui.getPropInt(Prop.QUALITY);
	    			DSScreen screen = ui.getPropEnum(Prop.PRIORITYSCREEN);
	    			int priority = ui.getPropInt(Prop.PRIORITYFACTOR);
	    			int qos = ui.getPropInt(Prop.QOS);
	    			ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
	    			
	    			// Initializes connection
	    			client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port, topScale, bottomScale, intrp);
				} catch (Exception e) {
					ui.displayError(e);
					return null;
				}
				break;
    		case CHIRUNOMOD:
    			try {
    				int quality = ui.getPropInt(Prop.QUALITY);
    				int capCpu = ui.getPropInt(Prop.CPUCAP);
    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
    				DSScreenBoth reqScreen = ui.getPropEnum(Prop.REQSCREEN);
    				boolean reqTGA = ui.getPropBoolean(Prop.REQTGA);
    				boolean interlace = ui.getPropBoolean(Prop.INTERLACE);
    				boolean vsync = ui.getPropBoolean(Prop.VSYNC);
    				
    				// Initializes connection
    				client = new ChirunoModClient(ip, quality, reqTGA, interlace, vsync, capCpu, colorMode, port, reqScreen, topScale, bottomScale, intrp);
    			} catch (Exception e) {
    				ui.displayError(e);
    				return null;
    			}
    			break;
    		case HZMOD:
    			try {
    				int quality = ui.getPropBoolean(Prop.REQTGA) ? 0 : ui.getPropInt(Prop.QUALITY);
    				int capCpu = ui.getPropInt(Prop.CPUCAP);
    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
    				
    				// Initializes connection
    				client = new HZModClient(ip, quality, capCpu, colorMode, port, topScale, bottomScale, intrp);
    			} catch (Exception e) {
    				ui.displayError(e);
    				return null;
    			}
    			break;
    	}
    	return client;
	}
	
	public static void initializeSwing(SettingsUI ui) {
		StreamingInterface client = initialize(ui);
		if(client != null) {
			Layout layout = ui.getPropEnum(Prop.LAYOUT);
			double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			new SwingVideo(client, layout, topScale, bottomScale);
		}
	}
	
	public static void initializeFile(SettingsUI ui) {
		StreamingInterface client = initialize(ui);
		if(client != null) {
			Layout layout = ui.getPropEnum(Prop.LAYOUT);
			String fileName = ui.getPropString(Prop.VIDEOFILE);
			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC);
			double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			new OutputFileVideo(client, layout, fileName+"."+vf.getExtension(), vf, topScale, bottomScale);
		}
	}
	
	public static void initializeSequence(SettingsUI ui) {
		StreamingInterface client = initialize(ui);
		if(client != null) {
			String dir = ui.getPropString(Prop.SEQUENCEDIR);
			new ImageSequenceVideo(client, dir, ui.getPropString(Prop.SEQUENCEPREFIX));
		}
	}
}
