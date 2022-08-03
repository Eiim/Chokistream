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
				}
				break;
    		case HZMOD:
    			try {
    				int quality = scene.getQuality();
    				int capCpu = scene.getCapCPU();
    				ColorMode colorMode = scene.getColorMode();
    				
    				// Initializes connection
    				client = new HZModClient(ip, quality, capCpu, colorMode);
    				output = new JavaFXVideo(client, layout);
    				stage.close();
    			} catch (Exception e) {
    				scene.displayError(e);
    			}
    	}
    }

}