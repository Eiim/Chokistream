package chokistream;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/*
 * This is a super bare-bones GUI, just type in an IP and select your mod
 * Could be used for an early release or something where we don't support all of the features of the big UI
 */
public class OldGUI extends SettingsGUI {
	
	private TextField tf;
	private ChoiceBox<String> cb;
	
	public OldGUI(App app) {
		super(new Pane(), 400, 200);
		
		tf = new TextField();
    	cb = new ChoiceBox<>();
    	cb.getItems().addAll("NTR", "HzMod");
    	cb.setValue("NTR");
    	
        Button b = new Button();
        b.setText("Connect!");
        b.setOnAction((e) -> {
        	app.connect();
        });
        
        tf.relocate(25,25);
        cb.relocate(25, 75);
        b.relocate(25, 125);
        
        Pane p = new Pane();
        p.getChildren().addAll(tf, cb, b);
        this.setRoot(p);
	}
	
	public String getIp() {
		return tf.getText();
	}
	
	public Mod getMod() throws InvalidOptionException {
		String mod = cb.getValue();
		switch(mod) {
			case "NTR":
				return Mod.NTR;
			case "HzMod":
				return Mod.HZMOD;
			default:
				throw new InvalidOptionException("Streamping App", mod);
		}
	}
}
