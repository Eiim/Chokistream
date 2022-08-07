package chokistream;

import java.util.Arrays;
import java.util.List;

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
			Mod mod;
	    	String ip;
	    	Layout layout;
	    	int port;
	    	try {
				mod = ui.getMod();
				ip = ui.getIp();
				layout = ui.getLayout();
				port = ui.getPort();
			} catch (InvalidOptionException e) {
				ui.displayError(e);
				return;
			}
	    	
	    	switch(mod) {
	    		case NTR:
					try {
						int quality = ui.getQuality();
		    			NTRScreen screen = ui.getScreen();
		    			int priority = ui.getPriority();
		    			int qos = ui.getQos();
		    			ColorMode colorMode = ui.getColorMode();
		    			
		    			// Initializes connection
		    			StreamingInterface client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port);
		    			new OutputFileVideo(client, layout, "out.mov");
					} catch (Exception e) {
						ui.displayError(e);
					}
					break;
	    		case CHOKIMOD:
	    		case HZMOD:
	    			try {
	    				int quality = ui.getQuality();
	    				int capCpu = ui.getCapCPU();
	    				ColorMode colorMode = ui.getColorMode();
	    				
	    				// Initializes connection
	    				StreamingInterface client = new HZModClient(ip, quality, capCpu, colorMode, port);
	    				new OutputFileVideo(client, layout, "out.mov");
	    			} catch (Exception e) {
	    				ui.displayError(e);
	    			}
	    	}
		} else {
			App.main(args);
		}
	}
}
