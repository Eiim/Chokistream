package chokistream;

import chokistream.props.OutputFormat;
import chokistream.props.Prop;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


public class App extends Application {
	
	private SettingsUI ui;
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
    	launch();
    }
    
    /**
     * Triggered from the UI. Attempts to instantiate the connection to the 3DS.
     */
    public void connect() {
		OutputFormat outForm = ui.getPropEnum(Prop.OUTPUTFORMAT, OutputFormat.class);
		
    	switch(outForm) {
    		case VISUAL:
    			Main.initializeJFX(this, ui);
    			break;
    		case FILE:
    			Main.initializeFile(ui);
    			break;
    	}
    	
    	stage.close();
    }
    
    public void reopen() {
    	stage.show();
    }

}