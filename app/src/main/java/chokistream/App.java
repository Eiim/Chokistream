package chokistream;

import java.io.File;
import java.io.FileNotFoundException;

import chokistream.INIParser.IniParseException;
import chokistream.Logger.LogLevel;
import chokistream.Logger.LogMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


public class App extends Application {
	
	private SettingsGUI scene;
	private StreamingInterface client;
	private VideoOutputInterface output;
	private Stage stage;
	private static final Logger logger = Logger.INSTANCE;
	
    /**
     * Adds the basic elements to a JavaFX stage. Triggered via main.
     * 
     * @param stage	The Stage to display on.
     */
    @Override
    public void start(Stage stage) throws Exception {
    	scene = new SnickerstreamGUI(this);
        stage.setScene(scene);
        stage.setTitle("Chokistream");
        stage.setResizable(false);
        IconLoader.applyFavicon(stage);
        
        stage.show();
        this.stage = stage;
        
        stage.setOnCloseRequest((e) -> {
        	Platform.exit();
        	System.exit(0);
        });
    }
    
    /**
     * Starts a new instance of Chokistream.
     * 
     * @param args	Currently unused
     */
    public static void main(String[] args) {
    	// Set up logger before anything else
    	LogLevel level = LogLevel.REGULAR;
    	LogMode mode = LogMode.CONSOLE;
    	String logFile = "chokistream.log";
    	try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			String levelS = parser.getProperty("logLevel");
			switch(levelS) {
				case "verbose":
					level = LogLevel.VERBOSE;
			}
			String modeS = parser.getProperty("logMode");
			switch(modeS) {
				case "file":
					mode = LogMode.FILE;
					break;
				case "both":
					mode = LogMode.BOTH;
			}
			logFile = parser.getProperty("logFile");
		} catch (FileNotFoundException | IniParseException e) {
			System.out.println("No config found or config was unreadable");
			System.out.println("This is expected on first launch");
		}
    	logger.init(mode, level, logFile);
    	
    	launch();
    }
    
    /**
     * Triggered from the UI. Attempts to instantiate the connection to the 3DS.
     */
    public void connect() {
    	// These are universal, so get these first and then sort out the rest by mod.
    	// Technically quality could be here.
    	Mod mod;
    	String ip;
    	Layout layout;
    	int port;
    	try {
			mod = scene.getMod();
			ip = scene.getIp();
			layout = scene.getLayout();
			port = scene.getPort();
		} catch (InvalidOptionException e) {
			scene.displayError(e);
			return;
		}
    	
    	switch(mod) {
    		case NTR:
				try {
					int quality = scene.getQuality();
	    			NTRScreen screen = scene.getScreen();
	    			int priority = scene.getPriority();
	    			int qos = scene.getQos();
	    			ColorMode colorMode = scene.getColorMode();
	    			
	    			// Initializes connection
	    			client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port);
	    			output = new JavaFXVideo(client, layout);
	    			stage.close();
				} catch (Exception e) {
					scene.displayError(e);
				}
				break;
    		case HZMOD:
    			try {
    				int quality = scene.getQuality();
    				int capCpu = scene.getCapCPU();
    				ColorMode colorMode = scene.getColorMode();
    				
    				// Initializes connection
    				client = new HZModClient(ip, quality, capCpu, colorMode, port);
    				output = new JavaFXVideo(client, layout);
    				stage.close();
    			} catch (Exception e) {
    				scene.displayError(e);
    			}
    	}
    }

}