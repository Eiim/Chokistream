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

    @Override
    public void start(Stage stage) throws Exception {
    	useSSUI(stage);
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
    
    public void useSSUI(Stage stage) throws Exception {
    	
    	// Left Half
    	
    	Label psLab = new Label("Preset");
    	psLab.relocate(14, 191);
    	ChoiceBox<String> preset = new ChoiceBox<String>();
    	preset.relocate(111, 187);
    	preset.setPrefSize(175, 25);
    	
    	Label qsvLab = new Label("QoS Value");
    	qsvLab.relocate(14, 156);
    	TextField qosVal = new TextField();
    	qosVal.relocate(110, 152);
    	qosVal.setPrefSize(175,25);
    	
    	Label iqLab = new Label("Image Quality");
    	iqLab.relocate(14, 121);
    	TextField imgQual = new TextField();
    	imgQual.relocate(110, 117);
    	imgQual.setPrefSize(175,25);
    	
    	Label pfLab = new Label("Priority Factor");
    	pfLab.relocate(14, 86);
    	TextField priFac = new TextField();
    	priFac.relocate(110, 82);
    	priFac.setPrefSize(175,25);
    	
    	Label spLab = new Label("Screen Priority");
    	spLab.relocate(14, 51);
    	ChoiceBox<String> scrPri = new ChoiceBox<String>();
    	scrPri.relocate(111, 47);
    	scrPri.setPrefSize(175, 25);
    	
    	Label ipLab = new Label("3DS IP");
    	ipLab.relocate(14, 18);
    	TextField ip = new TextField();
    	ip.relocate(110, 14);
    	ip.setPrefSize(175,25);
    	ip.setPromptText("0.0.0.0");
    	
    	// Right Half
    	
    	Label saLab = new Label("Streaming App");
    	saLab.relocate(307, 18);
    	ChoiceBox<String> strApp = new ChoiceBox<String>();
    	strApp.relocate(411, 14);
    	strApp.setPrefSize(175, 25);
    	
    	Label intLab = new Label("Interpolation");
    	intLab.relocate(307, 52);
    	ChoiceBox<String> intrp = new ChoiceBox<String>();
    	intrp.relocate(411, 47);
    	intrp.setPrefSize(175, 25);
    	
    	Label slLab = new Label("Screen Layout");
    	slLab.relocate(307, 86);
    	ChoiceBox<String> layout = new ChoiceBox<String>();
    	layout.relocate(411, 82);
    	layout.setPrefSize(175, 25);
    	
    	Button about = new Button("About");
    	about.relocate(307, 117);
    	about.setPrefSize(90, 25);
    	
    	Button adv = new Button("Advanced");
    	adv.relocate(402, 117);
    	adv.setPrefSize(90, 25);
    	
    	Button patch = new Button("NFC Patch");
    	patch.relocate(495, 117);
    	patch.setPrefSize(90, 25);
    	
    	Button connect = new Button("Connect!");
    	connect.relocate(308, 152);
    	connect.setPrefSize(279, 61);
    	
    	Pane p = new Pane();
    	p.getChildren().addAll(psLab, preset, qsvLab, qosVal, iqLab, imgQual, pfLab, priFac, spLab, scrPri, ipLab, ip,
    			saLab, strApp, intLab, intrp, slLab, layout, about, adv, patch, connect);
    	
    	Scene scene = new Scene(p, 600, 225);
        stage.setScene(scene);
        stage.setTitle("Chokistream");
        stage.show();
    }

}