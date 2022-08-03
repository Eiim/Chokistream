package chokistream;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class SnickerstreamGUI extends SettingsGUI {
	private ChoiceBox<String> preset;
	private TextField qosVal;
	private TextField imgQual;
	private TextField priFac;
	private ChoiceBox<String> scrPri;
	private TextField ip;
	private ChoiceBox<String> strApp;
	private ChoiceBox<String> clrMod;
	private ChoiceBox<String> layout;
	private Button about;
	private Button adv;
	private Button patch;
	private Button connect;
	private Pane pane;
	
	// Advanced Settings
	
	private TextField port;
	private TextField topScale;
	private TextField bottomScale;
	private ChoiceBox<String> logOutput;
	private ChoiceBox<String> logLevel;
	private TextField logFile;
	private ChoiceBox<String> intrp;
	private TextField custDPI;
	private Button apply;
	private Scene advScene;
	
	public SnickerstreamGUI(App app) {
		super(new Pane(), 600, 226);
		
		// Left Half
		
		Label ipLab = new Label("3DS IP");
    	ipLab.relocate(14, 18);
    	ip = new TextField();
    	ip.relocate(110, 14);
    	ip.setPrefSize(175,25);
    	ip.setPromptText("0.0.0.0");
		
    	Label spLab = new Label("Screen Priority");
    	spLab.relocate(14, 51);
    	scrPri = new ChoiceBox<String>();
    	scrPri.relocate(111, 47);
    	scrPri.setPrefSize(175, 25);
    	scrPri.getItems().addAll("Top", "Bottom");
    	scrPri.setValue("Top");
    	
    	Label pfLab = new Label("Priority Factor");
    	pfLab.relocate(14, 86);
    	priFac = new TextField();
    	priFac.relocate(110, 82);
    	priFac.setPrefSize(175,25);
    	priFac.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	priFac.setText("8");
    	
    	Label iqLab = new Label("Image Quality");
    	iqLab.relocate(14, 121);
    	imgQual = new TextField();
    	imgQual.relocate(110, 117);
    	imgQual.setPrefSize(175,25);
    	imgQual.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	imgQual.setText("70");
    	
    	Label qsvLab = new Label("QoS Value");
    	qsvLab.relocate(14, 156);
    	qosVal = new TextField();
    	qosVal.relocate(110, 152);
    	qosVal.setPrefSize(175,25);
    	qosVal.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	qosVal.setText("26");  
    	
		Label psLab = new Label("Preset");
    	psLab.relocate(14, 191);
    	preset = new ChoiceBox<String>();
    	preset.relocate(111, 187);
    	preset.setPrefSize(175, 25);
    	preset.getItems().addAll("Best Quality", "Great Quality", "Good Quality", "Balanced", "Good Framerate", "Great Framerate", "Best Framerate");
    	preset.setOnAction((e) -> {
    		String val = ((ChoiceBox<String>)e.getTarget()).getValue();
    		switch(val) {
    			case "Best Quality":
    				priFac.setText("2");
    				imgQual.setText("90");
    				qosVal.setText("10");
    				break;
    			case "Great Quality":
    				priFac.setText("5");
    				imgQual.setText("80");
    				qosVal.setText("18");
    				break;
    			case "Good Quality":
    				priFac.setText("5");
    				imgQual.setText("75");
    				qosVal.setText("18");
    				break;
    			case "Balanced":
    				priFac.setText("5");
    				imgQual.setText("70");
    				qosVal.setText("20");
    				break;
    			case "Good Framerate":
    				priFac.setText("8");
    				imgQual.setText("60");
    				qosVal.setText("26");
    				break;
    			case "Great Framerate":
    				priFac.setText("8");
    				imgQual.setText("50");
    				qosVal.setText("26");
    				break;
    			case "Best Framerate":
    				priFac.setText("10");
    				imgQual.setText("40");
    				qosVal.setText("34");
    				break;
    		}
    	});
    	
    	// Right Half
    	
    	Label saLab = new Label("Streaming App");
    	saLab.relocate(307, 18);
    	strApp = new ChoiceBox<String>();
    	strApp.relocate(411, 14);
    	strApp.setPrefSize(175, 25);
    	strApp.getItems().addAll("NTR", "HzMod");
    	strApp.setValue("NTR");
    	strApp.setOnAction((e) -> {
    		
    	});
    	
    	Label clrLab = new Label("Color Mode");
    	clrLab.relocate(307, 52);
    	clrMod = new ChoiceBox<String>();
    	clrMod.relocate(411, 47);
    	clrMod.setPrefSize(175, 25);
    	clrMod.getItems().addAll("Regular", "VC Blue Shift (Test)", "Grayscale");
    	clrMod.setValue("Regular");
    	
    	
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
    	about.setOnAction((e) -> {
    		displayAbout();
    	});
    	
    	adv = new Button("Advanced");
    	adv.relocate(402, 117);
    	adv.setPrefSize(90, 25);
    	
    	patch = new Button("NFC Patch");
    	patch.relocate(495, 117);
    	patch.setPrefSize(90, 25);
    	patch.setDisable(true);
    	
    	connect = new Button("Connect!");
    	connect.relocate(308, 152);
    	connect.setPrefSize(279, 61);
    	connect.setOnAction((e) -> {
    		saveSettings();
    		app.connect();
    	});
    	
    	// Initialize advanced options so we can refer to them in the background
    	topScale = new TextField();
    	topScale.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
    	topScale.setText("1");
    	bottomScale = new TextField();
    	bottomScale.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
    	bottomScale.setText("1");
    	intrp = new ChoiceBox<String>();
    	intrp.getItems().addAll("Nearest Neighbor", "Smooth");
    	intrp.setValue("Nearest Neighbor");
    	custDPI = new TextField();
    	custDPI.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	custDPI.setText(""+Toolkit.getDefaultToolkit().getScreenResolution());
    	port = new TextField();
    	port.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	port.setText("8001");
    	logOutput = new ChoiceBox<String>();
    	logOutput.getItems().addAll("Console", "File", "Both");
    	logOutput.setValue("Console");
    	logLevel = new ChoiceBox<String>();
    	logLevel.getItems().addAll("Regular", "Verbose");
    	logLevel.setValue("Regular");
    	logFile = new TextField();
    	logFile.setText("chokistream.log");
    	
    	loadSettings();
    	
    	pane = new Pane();
    	pane.getChildren().addAll(ipLab, ip, spLab, scrPri, pfLab, priFac, iqLab, imgQual, qsvLab, qosVal, psLab, preset,
    			saLab, strApp, clrLab, clrMod, slLab, layout, about, adv, patch, connect);
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
	
	public ColorMode getColorMode() throws InvalidOptionException {
		String cm = clrMod.getValue();
		switch(cm) {
			case "Regular":
				return ColorMode.REGULAR;
			case "VC Blue Shift":
				return ColorMode.VC_BLUE_SHIFT;
			case "VC Blue Shift (Test)":
				return ColorMode.VC_BLUE_SHIFT;
			case "Grayscale":
				return ColorMode.GRAYSCALE;
			default:
				throw new InvalidOptionException("Color Mode", cm);
		}
	}
	
	public void saveSettings() {
		File f = new File("chokistream.ini");
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				displayError(e);
			}
		}
		try {
			INIParser parser = new INIParser(f);
			
			parser.setProperty("ip", getIp());
			parser.setProperty("qos", getQos());
			parser.setProperty("quality", getQuality());
			parser.setProperty("priority", getPriority());
			parser.setProperty("priorityScreen", scrPri.getValue());
			parser.setProperty("mod", strApp.getValue());
			parser.setProperty("layout", layout.getValue());
			parser.setProperty("colorMode", clrMod.getValue());
		} catch (Exception e) {
			displayError(e);
		}
	}
	
	public void loadSettings() {
		File f = new File("chokistream.ini");
		if(!f.exists()) {
			return;
		}
		try {
			INIParser parser = new INIParser(f);
			
			setTextIfProp(parser, ip, "ip");
			setTextIfProp(parser, qosVal, "qos");
			setTextIfProp(parser, imgQual, "quality");
			setTextIfProp(parser, priFac, "priority");
			setValueIfProp(parser, scrPri, "priorityScreen");
			setValueIfProp(parser, strApp, "mod");
			setValueIfProp(parser, layout, "layout");
			setValueIfProp(parser, clrMod, "colorMode");
		} catch (Exception e) {
			displayError(e);
		}
	}
	
	private void setTextIfProp(INIParser parser, TextField tf, String prop) {
		String val = parser.getProperty(prop);
		if(!val.equals("")) {
			tf.setText(val);
		}
	}
	
	private void setValueIfProp(INIParser parser, ChoiceBox<String> cb, String prop) {
		String val = parser.getProperty(prop);
		if(!val.equals("")) {
			cb.setValue(val);
		}
	}
	
	private void displayAbout() {
		ImageView logo = new ImageView();
		logo.setImage(IconLoader.get64x());
		logo.relocate(10, 10);
		Text name = new Text(84, 64, "Chokistream");
		Font f = Font.font("System", FontWeight.BOLD, 60);
		name.setFont(f);
		
		ScrollPane textScroll = new ScrollPane();
		textScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		textScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		Text t = new Text("Made by Eiim, herronjo, and ChainSwordCS.\r\n"
				+ "\r\n"
				+ "This software and its source code are provided under the GPL-3.0 License.  See LICENSE for the full license.\r\n"
				+ "\r\n"
				+ "Chokistream was made possible by the use and reference of several projects. Special thanks to:\r\n"
				+ " * RattletraPM for Snickerstream\r\n"
				+ " * Sono for HzMod\r\n"
				+ " * Cell9/44670 for BootNTR\r\n"
				+ " * Nanquitas for BootNTRSelector\r\n"
				+ " * All other open-source contributors");
		t.setWrappingWidth(400);
		textScroll.setContent(t);
		t.relocate(7, 7);
		textScroll.relocate(10, 84);
		textScroll.setPrefWidth(425);
		textScroll.setPrefHeight(300);
		
		Group g = new Group(logo, name, textScroll);
		Scene scene = new Scene(g, 500, 400);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		IconLoader.applyFavicon(stage);
		stage.show();
	}
}
