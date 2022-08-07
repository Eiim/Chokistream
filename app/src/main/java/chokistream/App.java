package chokistream;

import java.io.File;
import java.io.FileNotFoundException;

import chokistream.INIParser.IniParseException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


public class App extends Application {
	
	private SettingsUI ui;
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
    	ui = new SnickerstreamGUI();
        stage.setScene(((SnickerstreamGUI)ui).setup(this));
        stage.setTitle("Chokistream");
        stage.setResizable(false);
        IconLoader.applyFavicon(stage);
        
        stage.show();
        this.stage = stage;
        
        stage.setOnCloseRequest((e) -> {
        	logger.close();
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
				case "Verbose":
					level = LogLevel.VERBOSE;
			}
			String modeS = parser.getProperty("logMode");
			switch(modeS) {
				case "File":
					mode = LogMode.FILE;
					break;
				case "Both":
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
    	int dpi;
    	double topScale;
    	double bottomScale;
    	InterpolationMode intrp;
    	try {
			mod = ui.getMod();
			ip = ui.getIp();
			layout = ui.getLayout();
			port = ui.getPort();
			dpi = ui.getDPI();
			topScale = ui.getTopScale();
			bottomScale = ui.getBottomScale();
			intrp = ui.getIntrpMode();
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
	    			client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port);
	    			output = new JavaFXVideo(client, layout, dpi, topScale, bottomScale, intrp);
	    			stage.close();
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
    				client = new HZModClient(ip, quality, capCpu, colorMode, port);
    				output = new JavaFXVideo(client, layout, dpi, topScale, bottomScale, intrp);
    				stage.close();
    			} catch (Exception e) {
    				ui.displayError(e);
    			}
    	}
    }

}