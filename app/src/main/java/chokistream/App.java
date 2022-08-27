package chokistream;

import java.io.File;
import java.io.IOException;

import chokistream.INIParser.IniParseException;
import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.EnumProp;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;
import chokistream.props.Mod;
import chokistream.props.OutputFormat;
import chokistream.props.Prop;
import chokistream.props.VideoFormat;
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
    	ui = new ModFocusedGUI();
    	stage.setScene(((ModFocusedGUI)ui).setup(this));
    	//ui = new SnickerstreamGUI();
    	//stage.setScene(((SnickerstreamGUI)ui).setup(this));
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
			String ll = parser.getProp(Prop.LOGLEVEL);
			level = EnumProp.fromLongName(LogLevel.class, ll.equals("") ? Prop.LOGLEVEL.getDefault().getLongName() : ll);
			String lm = parser.getProp(Prop.LOGMODE);
			mode = EnumProp.fromLongName(LogMode.class, lm.equals("") ? Prop.LOGMODE.getDefault().getLongName() : lm);
			logFile = parser.getProperty(Prop.LOGFILE.getShortName());
		} catch (IOException | IniParseException e) {
			System.out.println("No config found or config was unreadable");
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
    	OutputFormat outForm;
    	try {
			mod = ui.getPropEnum(Prop.MOD, Mod.class);
			ip = ui.getPropString(Prop.IP);
			layout = ui.getPropEnum(Prop.LAYOUT, Layout.class);
			port = ui.getPropInt(Prop.PORT);
			dpi = ui.getPropInt(Prop.DPI);
			topScale = ui.getPropDouble(Prop.TOPSCALE);
			bottomScale = ui.getPropDouble(Prop.BOTTOMSCALE);
			intrp = ui.getPropEnum(Prop.INTRPMODE, InterpolationMode.class);
			outForm = ui.getPropEnum(Prop.OUTPUTFORMAT, OutputFormat.class);
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
	    			client = new NTRClient(ip, quality, screen, priority, qos, colorMode, port, topScale, bottomScale, intrp);
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
    				client = new CHokiModClient(ip, quality, capCpu, colorMode, port, reqScreen, topScale, bottomScale, intrp);
    			} catch (Exception e) {
    				ui.displayError(e);
    			}
    			break;
    		case HZMOD:
    			try {
    				int quality = ui.getPropInt(Prop.QUALITY);
    				int capCpu = ui.getPropInt(Prop.CPUCAP);
    				ColorMode colorMode = ui.getPropEnum(Prop.COLORMODE, ColorMode.class);
    				DSScreenBoth reqScreen = ui.getPropEnum(Prop.REQSCREEN, DSScreenBoth.class);
    				
    				// Initializes connection
    				client = new HZModClient(ip, quality, capCpu, colorMode, port, reqScreen, topScale, bottomScale, intrp);
    			} catch (Exception e) {
    				ui.displayError(e);
    			}
    	}
    	
    	switch(outForm) {
    		case VISUAL:
    			output = new JavaFXVideo(this, client, layout, dpi, topScale, bottomScale, intrp);
    			break;
    		case FILE:
    			String fileName = ui.getPropString(Prop.VIDEOFILE);
    			VideoFormat vf = ui.getPropEnum(Prop.VIDEOCODEC, VideoFormat.class);
    			output = new OutputFileVideo(client, layout, fileName+"."+vf.getExtension(), vf);
    	}
    	
    	stage.close();
    }
    
    public void reopen() {
    	stage.show();
    }

}