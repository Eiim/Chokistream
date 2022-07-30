package chokistream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class App extends Application {
	
	private SettingsGUI scene;
	private StreamingInterface client;
	
    @Override
    public void start(Stage stage) throws Exception {
    	scene = new SnickerstreamGUI(this);
        stage.setScene(scene);
        stage.setTitle("Chokistream");
        stage.show();
    }
    
    public static void main(String[] args) {
    	launch();
    }
    
    public void useTestUI(Stage stage) throws Exception {
    	TextField tf = new TextField();
    	ChoiceBox<String> cb = new ChoiceBox<>();
    	cb.getItems().addAll("NTR", "HzMod");
    	cb.setValue("NTR");
    	
        Button b = new Button();
        b.setText("Connect!");
        b.setOnAction((e) -> {
        	System.out.println("Text: "+tf.getText()+"\nCheckbox: "+cb.getValue());
        });
        
        tf.relocate(25,25);
        cb.relocate(25, 75);
        b.relocate(25, 125);
        
        Pane p = new Pane();
        p.getChildren().addAll(tf, cb, b);
        
        Scene scene = new Scene(p, 400, 200);
        stage.setScene(scene);
        stage.setTitle("Chokistream");
        stage.show();
    }
    
    public void connect() {
    	Mod mod;
    	String ip;
    	try {
			mod = scene.getMod();
			ip = scene.getIp();
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
	    			
	    			client = new NTRClient(ip, quality, screen, priority, qos);
				} catch (Exception e) {
					scene.displayError(e);
					return;
				}
    		case HZMOD:
    			try {
    				int quality = scene.getQuality();
    				int capCpu = scene.getCapCPU();
    				
    				client = new HZModClient(ip, quality, capCpu);
    			} catch (Exception e) {
    				scene.displayError(e);
    			}
    	}
    }

}