package chokistream;

import javafx.application.Application;
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