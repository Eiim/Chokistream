package chokistream;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class SnickerstreamGUI extends SettingsUI {
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
	
	// Advanced Settings
	
	private TextField port;
	private TextField topScale;
	private TextField bottomScale;
	private ChoiceBox<String> logMode;
	private ChoiceBox<String> logLevel;
	private TextField logFile;
	private ChoiceBox<String> intrp;
	private TextField custDPI;
	private TextField cpuCap;
	private Button apply;
	private Stage advStage;
	
	private static final Logger logger = Logger.INSTANCE;
	
	public Scene setup(App app) {
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
    		switch(strApp.getValue()) {
    			case "HzMod":
    				port.setText("6464");
    				cpuCap.setDisable(false);
    				break;
    			case "NTR":
    				port.setText("8000");
    				cpuCap.setDisable(true);
    				break;
    			default:
    				logger.log("Mod is not one of the expected mods!", LogLevel.VERBOSE);
    		}
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
    	adv.setOnAction((e) -> {
    		if(advStage == null || !advStage.isShowing())
    			displayAdvanced();
    	});
    	
    	patch = new Button("NFC Patch");
    	patch.relocate(495, 117);
    	patch.setPrefSize(90, 25);
    	patch.setOnAction((e) -> {
    		Button n3ds = new Button("New 3DS");
    		Button o3ds = new Button("Old 3DS");
    		n3ds.relocate(7, 7);
    		n3ds.setPrefSize(100, 25);
    		o3ds.relocate(114, 7);
    		o3ds.setPrefSize(100, 25);
    		
    		Group g = new Group(n3ds, o3ds);
    		Scene selectScene = new Scene(g, 221, 39);
    		Stage selectStage = new Stage();
    		selectStage.setScene(selectScene);
    		selectStage.setResizable(false);
    		IconLoader.applyFavicon(selectStage);
    		
    		n3ds.setOnAction((e2) -> {
    			try {
    				selectStage.close();
					NTRClient.sendNFCPatch(getIp(), getPropInt(Prop.PORT), null, ConsoleModel.N3DS);
				} catch (IOException | RuntimeException ex) {
					displayError(ex);
				}
    		});
    		o3ds.setOnAction((e2) -> {
    			try {
    				selectStage.close();
					NTRClient.sendNFCPatch(getIp(), getPropInt(Prop.PORT), null, ConsoleModel.O3DS);
				} catch (IOException | RuntimeException ex) {
					displayError(ex);
				}
    		});
    		
    		selectStage.show();
    	});
    	
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
    	intrp.getItems().addAll("None", "Smooth");
    	intrp.setValue("None");
    	custDPI = new TextField();
    	custDPI.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	custDPI.setText(""+Toolkit.getDefaultToolkit().getScreenResolution());
    	port = new TextField();
    	port.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	port.setText("8000");
    	logMode = new ChoiceBox<String>();
    	logMode.getItems().addAll("Console", "File", "Both");
    	logMode.setValue("Console");
    	logLevel = new ChoiceBox<String>();
    	logLevel.getItems().addAll("Regular", "Verbose");
    	logLevel.setValue("Regular");
    	logFile = new TextField();
    	logFile.setText("chokistream.log");
    	cpuCap = new TextField();
    	cpuCap.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    	cpuCap.setText("0");
    	
    	loadSettings();
    	
    	Pane pane = new Pane();
    	pane.getChildren().addAll(ipLab, ip, spLab, scrPri, pfLab, priFac, iqLab, imgQual, qsvLab, qosVal, psLab, preset,
    			saLab, strApp, clrLab, clrMod, slLab, layout, about, adv, patch, connect);
    	return new Scene(pane, 600, 225);
	}
	
	public int getPropInt(Prop<Integer> p) {
		if(p.equals(Prop.QUALITY)) {
			int quality = Integer.parseInt(imgQual.getText());
			if(quality < 0 || quality > 100)
				displayPropWarn(p, quality);
			return quality;
		} else if(p.equals(Prop.PRIORITYFACTOR)) {
			int priority = Integer.parseInt(priFac.getText());
			if(priority < 0 || priority > 100)
				displayPropWarn(p, priority);
			return priority;
		} else if(p.equals(Prop.QOS)) {
			int qos = Integer.parseInt(qosVal.getText());
			if(qos < 0 || qos > 100)
				displayPropWarn(p, qos);
			return qos;
		} else if(p.equals(Prop.PORT)) {
			int portVal = Integer.parseInt(port.getText());
			if(portVal != 8000 && portVal != 6464)
				displayPropWarn(p, portVal);
			return portVal;
		} else if(p.equals(Prop.DPI)) {
			int dpi = Integer.parseInt(custDPI.getText());
			if(dpi < 48 || dpi > 480)
				displayPropWarn(p, dpi);
			return dpi;
		} else if(p.equals(Prop.CPUCAP)) {
			int capCPU = Integer.parseInt(cpuCap.getText());
			if(capCPU < 0 || capCPU > 100)
				displayPropWarn(p, capCPU);
			return capCPU;
		} else {
			return p.getDefault();
		}
	}
	
	public String getPropString(Prop<String> p) {
		if(p.equals(Prop.IP)) {
			return ip.getText();
		} else if(p.equals(Prop.LOGFILE)) {
			return logFile.getText();
		} else {
			return p.getDefault();
		}
	}
	
	public double getPropDouble(Prop<Double> p) {
		if(p.equals(Prop.TOPSCALE)) {
			double scale = Double.parseDouble(topScale.getText());
			if(scale <= 0 || scale > 10)
				displayPropWarn(p, scale);
			return scale;
		} else if (p.equals(Prop.BOTTOMSCALE)) {
			double scale = Double.parseDouble(bottomScale.getText());
			if(scale <= 0 || scale > 10)
				displayPropWarn(p, scale);
			return scale;
		} else {
			return p.getDefault();
		}
	}
	
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p, Class<T> c) {
		if(p.equals(Prop.MOD)) {
			String mod = strApp.getValue();
			return EnumProp.fromLongName(c, mod);
		} else if(p.equals(Prop.PRIORITYSCREEN)) {
			String scr = scrPri.getValue();
			return EnumProp.fromLongName(c, scr);
		} else if(p.equals(Prop.LOGMODE)) {
			String mode = logMode.getValue();
			return EnumProp.fromLongName(c, mode);
		} else if(p.equals(Prop.LOGLEVEL)) {
			String level = logLevel.getValue();
			return EnumProp.fromLongName(c, level);
		} else if(p.equals(Prop.INTRPMODE)) {
			String imode = intrp.getValue();
			return EnumProp.fromLongName(c, imode);
		} else if(p.equals(Prop.LAYOUT)) {
			String lay = layout.getValue();
			return EnumProp.fromLongName(c, lay);
		} else if(p.equals(Prop.COLORMODE)) {
			String cm = clrMod.getValue();
			return EnumProp.fromLongName(c, cm);
		} else {
			return p.getDefault();
		}
	}
	
	public String getIp() {
		return ip.getText();
	}
	
	public void displayPropWarn(Prop<?> p, Object val) {
		// TODO: maybe improve in future?
		logger.log("Warning: "+p.getLongName()+" has bad value "+val.toString());
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
			
			parser.setProp(Prop.IP, getPropString(Prop.IP));
			parser.setProp(Prop.QOS, getPropInt(Prop.QOS));
			parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
			parser.setProp(Prop.PRIORITYFACTOR, getPropInt(Prop.PRIORITYFACTOR));
			parser.setProp(Prop.PRIORITYSCREEN, getPropEnum(Prop.PRIORITYSCREEN, DSScreen.class));
			parser.setProp(Prop.MOD, getPropEnum(Prop.MOD, Mod.class));
			parser.setProp(Prop.LAYOUT, getPropEnum(Prop.LAYOUT, Layout.class));
			parser.setProp(Prop.COLORMODE, getPropEnum(Prop.COLORMODE, ColorMode.class));
			
			parser.setProp(Prop.PORT, getPropInt(Prop.PORT));
			parser.setProp(Prop.TOPSCALE, getPropDouble(Prop.TOPSCALE));
			parser.setProp(Prop.BOTTOMSCALE, getPropDouble(Prop.BOTTOMSCALE));
			parser.setProp(Prop.LOGMODE, getPropEnum(Prop.LOGMODE, LogMode.class));
			parser.setProp(Prop.LOGLEVEL, getPropEnum(Prop.LOGLEVEL, LogLevel.class));
			parser.setProp(Prop.LOGFILE, getPropString(Prop.LOGFILE));
			parser.setProp(Prop.INTRPMODE, getPropEnum(Prop.INTRPMODE, InterpolationMode.class));
			parser.setProp(Prop.DPI, getPropInt(Prop.DPI));
			parser.setProp(Prop.CPUCAP, getPropInt(Prop.CPUCAP));
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
			
			setTextIfProp(parser, port, "port");
			setTextIfProp(parser, topScale, "topScale");
			setTextIfProp(parser, bottomScale, "bottomScale");
			setValueIfProp(parser, logMode, "logMode");
			setValueIfProp(parser, logLevel, "logLevel");
			setTextIfProp(parser, logFile, "logFile");
			setValueIfProp(parser, intrp, "interpolationMode");
			setTextIfProp(parser, custDPI, "dpi");
			setTextIfProp(parser, cpuCap, "cpuCap");
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
	
	private void displayAdvanced() {
		// Left column
		
		Label topLab = new Label("Top Scale");
		topLab.relocate(14, 15);
		topScale.relocate(106, 10);
		
		Label botLab = new Label("Bottom Scale");
		botLab.relocate(14, 48);
		bottomScale.relocate(106, 43);
		
		Label intrpLab = new Label("Interpolation");
		intrpLab.relocate(14, 81);
		intrp.relocate(106, 76);
		intrp.setPrefWidth(150);
		
		Label dpiLab = new Label("Custom DPI");
		dpiLab.relocate(14, 115);
		custDPI.relocate(106, 110);
		
		Label portLab = new Label("3DS Port");
		portLab.relocate(14, 147);
		port.relocate(106, 142);
		
		// Right column
		
		Label logModeLab = new Label("Log Output");
		logModeLab.relocate(271, 15);
		logMode.relocate(363, 10);
		logMode.setPrefWidth(150);
		
		Label logLevelLab = new Label("Log Level");
		logLevelLab.relocate(271, 48);
		logLevel.relocate(363, 43);
		logLevel.setPrefWidth(150);
		
		Label logFileLab = new Label("Log File");
		logFileLab.relocate(271, 81);
		logFile.relocate(363, 76);
		
		Label cpuCapLab = new Label("CPU Cap");
		cpuCapLab.relocate(271, 115);
		cpuCap.relocate(363, 110);
		
		apply = new Button("Apply");
		apply.relocate(271, 142);
		apply.setPrefSize(242, 25);
		apply.setOnAction((e) -> {
			try {
				logger.setLevel(getPropEnum(Prop.LOGLEVEL, LogLevel.class));
				logger.setMode(getPropEnum(Prop.LOGMODE, LogMode.class));
				logger.setFile(getPropString(Prop.LOGFILE));
			} catch(RuntimeException ioe) {
				displayError(ioe);
			}
			advStage.close();
		});
		
		Pane advPane = new Pane();
		advPane.getChildren().addAll(topLab, topScale, botLab, bottomScale, intrpLab, intrp, dpiLab, custDPI, portLab, port,
				logModeLab, logMode, logLevelLab, logLevel, logFileLab, logFile, cpuCapLab, cpuCap, apply);
		Scene advScene = new Scene(advPane, 526, 181);
		advStage = new Stage();
		advStage.setScene(advScene);
		IconLoader.applyFavicon(advStage);
		advStage.show();
	}
	
	// Generic popup
	public void displayError(Exception e) {
		Stage popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		Label message = new Label(e.getClass().getSimpleName()+": "+e.getMessage());
		message.setPadding(new Insets(7));
		Scene scene = new Scene(message);
		popup.setScene(scene);
		popup.setTitle("Error");
		IconLoader.applyFavicon(popup);
		popup.show();
	}
}
