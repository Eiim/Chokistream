package chokistream;

import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

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
	private static JFrame top, bottom, both;
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
			top = new JFrame();
			bottom = new JFrame();
			both = new JFrame();
			new SwingGUI();
		}
	}
	
	private static StreamingInterface initialize(SettingsUI ui) {
		// These are universal, so get these first and then sort out the rest by mod.
		// Quality could be here except the logic is different in CHM.
    	Mod mod;
    	String ip;
    	int port;
    	try {
			mod = ui.getPropEnum(Prop.MOD);
			ip = ui.getPropString(Prop.IP);
			port = ui.getPropInt(Prop.PORT);
		} catch (RuntimeException e) {
			ui.displayError(e);
			return null;
		}
    	
    	switch(mod) {
    		case NTR:
				try {
					int quality = ui.getPropInt(Prop.QUALITY);
	    			DSScreen screen = ui.getPropEnum(Prop.PRIORITYSCREEN);
	    			int priority = ui.getPropInt(Prop.PRIORITYFACTOR);
	    			int qos = ui.getPropInt(Prop.QOS);
	    			ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
	    			
	    			// Initializes connection
	    			return new NTRClient(ip, quality, screen, priority, qos, colorMode, port);
				} catch (Exception e) {
					ui.displayError(e);
					return null;
				}
    		case CHIRUNOMOD:
    			try {
    				int quality = ui.getPropInt(Prop.QUALITY);
    				int capCpu = ui.getPropInt(Prop.CPUCAP);
    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
    				DSScreenBoth reqScreen = ui.getPropEnum(Prop.REQSCREEN);
    				boolean reqTGA = ui.getPropBoolean(Prop.REQTGA);
    				boolean interlace = ui.getPropBoolean(Prop.INTERLACE);
    				
    				// Initializes connection
    				return new ChirunoModClient(ip, quality, reqTGA, interlace, capCpu, colorMode, port, reqScreen);
    			} catch (Exception e) {
    				ui.displayError(e);
    				return null;
    			}
    		case HZMOD:
    			try {
    				int quality = ui.getPropBoolean(Prop.REQTGA) ? 0 : ui.getPropInt(Prop.QUALITY);
    				int capCpu = ui.getPropInt(Prop.CPUCAP);
    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE);
    				
    				// Initializes connection
    				return new HZModClient(ip, quality, capCpu, colorMode, port);
    			} catch (Exception e) {
    				ui.displayError(e);
    				return null;
    			}
    	}
    	return null; // Shouldn't ever get here
	}
	
	public static void initializeSwing(SettingsUI ui) {
		System.out.println("Initializing client");
		StreamingInterface client = initialize(ui);
		if(client != null) {
			System.out.println("Getting properties");
			Layout layout = ui.getPropEnum(Prop.LAYOUT);
			InterpolationMode intrp = ui.getPropEnum(Prop.INTRPMODE);
			double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			ChokiKeybinds ck = ui.getKeybinds();
			boolean showFPS = ui.getPropBoolean(Prop.SHOWFPS);
			System.out.println("Starting Video");
			new SwingVideo(client, layout, topScale, bottomScale, intrp, ck, showFPS, top, bottom, both);
		}
	}
	
	public static void initializeFile(SettingsUI ui) {
		StreamingInterface client = initialize(ui);
		if(client != null) {
			Layout layout = ui.getPropEnum(Prop.LAYOUT);
			String fileName = ui.getPropString(Prop.VIDEOFILE);
			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC);
			InterpolationMode intrp = ui.getPropEnum(Prop.INTRPMODE);
			double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			new OutputFileVideo(client, layout, fileName, vf.getExtension(), vf, intrp, topScale, bottomScale);
		}
	}
	
	public static void initializeSequence(SettingsUI ui) {
		StreamingInterface client = initialize(ui);
		if(client != null) {
			String dir = ui.getPropString(Prop.SEQUENCEDIR);
			String prefix = ui.getPropString(Prop.SEQUENCEPREFIX);
			InterpolationMode intrp = ui.getPropEnum(Prop.INTRPMODE);
			Layout layout = ui.getPropEnum(Prop.LAYOUT);
			double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			new ImageSequenceVideo(client, dir, prefix, intrp, layout, topScale, bottomScale);
		}
	}
}
