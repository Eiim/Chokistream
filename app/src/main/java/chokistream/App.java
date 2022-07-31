package chokistream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class App extends Application {
	
	private SettingsGUI scene;
	private StreamingInterface client;
	private VideoOutputInterface output;
	private Stage stage;
	
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
        
        try {
	        Image logo64 = new Image(getClass().getResourceAsStream("/res/logo64.png"));
	        Image logo48 = new Image(getClass().getResourceAsStream("/res/logo48.png"));
	        Image logo32 = new Image(getClass().getResourceAsStream("/res/logo32.png"));
	        Image logo16 = new Image(getClass().getResourceAsStream("/res/logo16.png"));
	        stage.getIcons().addAll(logo16, logo32, logo48, logo64);
        } catch(NullPointerException e) {
        	System.out.println("Couldn't find icons, most likely not running from jar");
        }
        
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
    	try {
			mod = scene.getMod();
			ip = scene.getIp();
			layout = scene.getLayout();
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
	    			client = new NTRClient(ip, quality, screen, priority, qos, colorMode);
	    			output = new JavaFXVideo(client, layout);
	    			stage.close();
				} catch (Exception e) {
					scene.displayError(e);
					return;
				}
				break;
    		case HZMOD:
    			try {
    				int quality = scene.getQuality();
    				int capCpu = scene.getCapCPU();
    				
    				// Initializes connection
    				client = new HZModClient(ip, quality, capCpu);
    				output = new JavaFXVideo(client, layout);
    				stage.close();
    			} catch (Exception e) {
    				scene.displayError(e);
    			}
    	}
    }

}