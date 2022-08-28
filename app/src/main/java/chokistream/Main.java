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
 * This is a wrapper class for App.
 * JavaFX normally runs as a module and hooks into the main class, but this configuration
 * would prevent us from building a jar with JavaFX included on the classpath. You'd instead
 * need to run a custom JDK or some such on the machine, which obviously isn't an option.
 * Instead, we use this as a wrapper, disconnected from JavaFX, which allows it to be loaded
 * properly, so we can compile a nice standalone jar. This apparently isn't supported but works.
 * 
 * We now can also parse CLI flags here to run headless using SettingsUI and OutputFileVideo.
 */
public class Main {
	public static void main(String[] args) {
		List<String> argsAL = Arrays.asList(args);
		if(argsAL.contains("--console") || argsAL.contains("-c")) {
			SettingsUI ui = new ConfigFileCLI();
			
			LogLevel level = ui.getPropEnum(Prop.LOGLEVEL, LogLevel.class);
			LogMode mode = ui.getPropEnum(Prop.LOGMODE, LogMode.class);
			String logFile = ui.getPropString(Prop.LOGFILE);
	    	Logger.INSTANCE.init(mode, level, logFile);
			
			Mod mod;
	    	String ip;
	    	Layout layout;
	    	int port;
	    	double topScale = ui.getPropDouble(Prop.TOPSCALE);
			double bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			InterpolationMode intrp = ui.getPropEnum(Prop.INTRPMODE, InterpolationMode.class);
	    	try {
				mod = ui.getPropEnum(Prop.MOD, Mod.class);
				ip = ui.getPropString(Prop.IP);
				layout = ui.getPropEnum(Prop.LAYOUT, Layout.class);
				port = ui.getPropInt(Prop.PORT);
			} catch (RuntimeException e) {
				ui.displayError(e);
				return;
			}
	    	
	    	switch(mod) {
	    		case NTR:
					try {
						int quality = ui.getPropInt(Prop.QUALITY);
		    			DSScreen screen = ui.getPropEnum(Prop.PRIORITYSCREEN, DSScreen.class);
		    			int priority = ui.getPropInt(Prop.PRIORITYFACTOR);
		    			int qos = ui.getPropInt(Prop.QOS);
		    			ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE, ColorMode.class);
		    			
		    			// Initializes connection
		    			StreamingInterface client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port, topScale, bottomScale, intrp);
		    			String fileName = ui.getPropString(Prop.VIDEOFILE);
		    			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC, VideoFormat.class);
		    			new OutputFileVideo(client, layout, fileName+"."+vf.getExtension(), vf);
					} catch (Exception e) {
						ui.displayError(e);
					}
					break;
	    		case CHOKIMOD:
	    			try {
	    				int quality = ui.getPropInt(Prop.QUALITY);
	    				int capCpu = ui.getPropInt(Prop.CPUCAP);
	    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE, ColorMode.class);
	    				DSScreenBoth reqScreen = ui.getPropEnum(Prop.REQSCREEN, DSScreenBoth.class);
	    				
	    				// Initializes connection
	    				StreamingInterface client = new CHokiModClient(ip, quality, capCpu, colorMode, port, reqScreen, topScale, bottomScale, intrp);
	    				String fileName = ui.getPropString(Prop.VIDEOFILE);
	        			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC, VideoFormat.class);
	        			new OutputFileVideo(client, layout, fileName+"."+vf.getExtension(), vf);
	    			} catch (Exception e) {
	    				ui.displayError(e);
	    			}
	    			break;
	    		case HZMOD:
	    			try {
	    				int quality = ui.getPropInt(Prop.QUALITY);
	    				int capCpu = ui.getPropInt(Prop.CPUCAP);
	    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE, ColorMode.class);
	    				
	    				// Initializes connection
	    				StreamingInterface client = new HZModClient(ip, quality, capCpu, colorMode, port, topScale, bottomScale, intrp);
	    				String fileName = ui.getPropString(Prop.VIDEOFILE);
	        			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC, VideoFormat.class);
	        			new OutputFileVideo(client, layout, fileName+"."+vf.getExtension(), vf);
	    			} catch (Exception e) {
	    				ui.displayError(e);
	    			}
	    	}
		} else {
			if(System.console() == null) {
				// TODO: make custom console
			}
			App.main(args);
		}
	}
}
