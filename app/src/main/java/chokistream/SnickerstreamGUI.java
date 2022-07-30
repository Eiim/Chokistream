package chokistream;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.util.converter.IntegerStringConverter;

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
		
		// Left Half
		
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
    	qosVal.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	qosVal.setText("26");
    	
    	Label iqLab = new Label("Image Quality");
    	iqLab.relocate(14, 121);
    	imgQual = new TextField();
    	imgQual.relocate(110, 117);
    	imgQual.setPrefSize(175,25);
    	imgQual.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	imgQual.setText("70");
    	
    	Label pfLab = new Label("Priority Factor");
    	pfLab.relocate(14, 86);
    	priFac = new TextField();
    	priFac.relocate(110, 82);
    	priFac.setPrefSize(175,25);
    	priFac.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
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
    	layout.getItems().addAll("Separate", "Vertical", "Vertical (Inv)", "Horizontal", "Horizontal (Inv)", "Top Only", "Bottom Only");
    	layout.setValue("Separate");
    	
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
		int quality = Integer.parseInt(imgQual.getText());
		if(quality < 0 || quality > 100) {
			throw new InvalidOptionException("Image Quality", imgQual.getText());
		}
		return quality;
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
		int priority = Integer.parseInt(priFac.getText());
		if(priority < 0 || priority > 100) {
			throw new InvalidOptionException("Priority Factor", priFac.getText());
		}
		return priority;
	}
	
	public int getQos() throws InvalidOptionException {
		int qos = Integer.parseInt(qosVal.getText());
		if(qos < 0 || qos > 100) {
			throw new InvalidOptionException("QoS Value", qosVal.getText());
		}
		return qos;
	}
	
	public Layout getLayout() throws InvalidOptionException {
		String lay = layout.getValue();
		switch(lay) {
			case "Separate":
				return Layout.SEPARATE;
			case "Vertical":
				return Layout.VERTICAL;
			case "Horizontal":
				return Layout.HORIZONTAL;
			case "Vertical (Inv)":
				return Layout.VERTICAL_INV;
			case "Horizontal (Inv)":
				return Layout.HORIZONTAL_INV;
			case "Top Only":
				return Layout.TOP_ONLY;
			case "Bottom Only":
				return Layout.BOTTOM_ONLY;
			default:
				throw new InvalidOptionException("Layout", lay);
		}
	}
}
