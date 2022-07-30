package chokistream;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class SnickerstreamGUI extends SettingsGUI {
	private ChoiceBox<String> preset;
	private TextField qosVal;
	private TextField imgQual;
	private TextField priFac;
	private ChoiceBox<String> scrPri;
	private TextField ip;
	private ChoiceBox<String> strApp;
	private ChoiceBox<String> intrp;
	private ChoiceBox<String> layout;
	private Button about;
	private Button adv;
	private Button patch;
	private Button connect;
	private Pane pane;
	
	public SnickerstreamGUI(App app) {
		super(new Pane(), 600, 255);
		
		Label psLab = new Label("Preset");
    	psLab.relocate(14, 191);
    	preset = new ChoiceBox<String>();
    	preset.relocate(111, 187);
    	preset.setPrefSize(175, 25);
    	
    	Label qsvLab = new Label("QoS Value");
    	qsvLab.relocate(14, 156);
    	qosVal = new TextField();
    	qosVal.relocate(110, 152);
    	qosVal.setPrefSize(175,25);
    	qosVal.setText("26");
    	
    	Label iqLab = new Label("Image Quality");
    	iqLab.relocate(14, 121);
    	imgQual = new TextField();
    	imgQual.relocate(110, 117);
    	imgQual.setPrefSize(175,25);
    	imgQual.setText("70");
    	
    	Label pfLab = new Label("Priority Factor");
    	pfLab.relocate(14, 86);
    	priFac = new TextField();
    	priFac.relocate(110, 82);
    	priFac.setPrefSize(175,25);
    	priFac.setText("8");
    	
    	Label spLab = new Label("Screen Priority");
    	spLab.relocate(14, 51);
    	scrPri = new ChoiceBox<String>();
    	scrPri.relocate(111, 47);
    	scrPri.setPrefSize(175, 25);
    	scrPri.getItems().addAll("Top", "Bottom");
    	scrPri.setValue("Top");
    	
    	Label ipLab = new Label("3DS IP");
    	ipLab.relocate(14, 18);
    	ip = new TextField();
    	ip.relocate(110, 14);
    	ip.setPrefSize(175,25);
    	ip.setPromptText("0.0.0.0");
    	
    	// Right Half
    	
    	Label saLab = new Label("Streaming App");
    	saLab.relocate(307, 18);
    	strApp = new ChoiceBox<String>();
    	strApp.relocate(411, 14);
    	strApp.setPrefSize(175, 25);
    	strApp.getItems().addAll("NTR", "HzMod");
    	strApp.setValue("NTR");
    	
    	Label intLab = new Label("Interpolation");
    	intLab.relocate(307, 52);
    	intrp = new ChoiceBox<String>();
    	intrp.relocate(411, 47);
    	intrp.setPrefSize(175, 25);
    	
    	Label slLab = new Label("Screen Layout");
    	slLab.relocate(307, 86);
    	layout = new ChoiceBox<String>();
    	layout.relocate(411, 82);
    	layout.setPrefSize(175, 25);
    	
    	about = new Button("About");
    	about.relocate(307, 117);
    	about.setPrefSize(90, 25);
    	
    	adv = new Button("Advanced");
    	adv.relocate(402, 117);
    	adv.setPrefSize(90, 25);
    	
    	patch = new Button("NFC Patch");
    	patch.relocate(495, 117);
    	patch.setPrefSize(90, 25);
    	
    	connect = new Button("Connect!");
    	connect.relocate(308, 152);
    	connect.setPrefSize(279, 61);
    	connect.setOnAction((e) -> {
    		app.connect();
    	});
    	
    	pane = new Pane();
    	pane.getChildren().addAll(psLab, preset, qsvLab, qosVal, iqLab, imgQual, pfLab, priFac, spLab, scrPri, ipLab, ip,
    			saLab, strApp, intLab, intrp, slLab, layout, about, adv, patch, connect);
    	this.setRoot(pane);
	}
	
	public String getIp() {
		return ip.getText();
	}
	
	public Mod getMod() throws InvalidOptionException {
		String mod = strApp.getValue();
		switch(mod) {
			case "NTR":
				return Mod.NTR;
			case "HzMod":
				return Mod.HZMOD;
			default:
				throw new InvalidOptionException("Streamping App", mod);
		}
	}
	
	public int getQuality() throws InvalidOptionException {
		try {
			return Integer.parseInt(imgQual.getText());
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("Image Quality", imgQual.getText());
		}
	}
	
	public NTRScreen getScreen() throws InvalidOptionException {
		String scr = scrPri.getValue();
		switch(scr) {
			case "Top":
				return NTRScreen.TOP;
			case "Bottom":
				return NTRScreen.BOTTOM;
			default:
				throw new InvalidOptionException("Preferred Screen", scr);
		}
	}
	
	public int getPriority() throws InvalidOptionException {
		try {
			return Integer.parseInt(priFac.getText());
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("Priority Factor", priFac.getText());
		}
	}
	
	public int getQos() throws InvalidOptionException {
		try {
			return Integer.parseInt(qosVal.getText());
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("QoS Value", qosVal.getText());
		}
	}
}
