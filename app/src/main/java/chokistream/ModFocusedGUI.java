package chokistream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import chokistream.INIParser.IniParseException;
import chokistream.props.ColorMode;
import chokistream.props.ConsoleModel;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.EnumProp;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;
import chokistream.props.Mod;
import chokistream.props.OutputFormat;
import chokistream.props.Prop;
import chokistream.props.VideoFormat;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class ModFocusedGUI extends SettingsUI {
	
	private ChoiceBox<String> mod;
	private TextField ip;
	private ChoiceBox<String> layout;
	private TextField topScale;
	private TextField bottomScale;
	private ChoiceBox<String> intrpMode;
	private TextField dpi;
	private ChoiceBox<String> colorMode;
	private TextField port;
	private ChoiceBox<String> logMode;
	private ChoiceBox<String> logLevel;
	private TextField logFile;
	private ChoiceBox<String> outputFormat;
	private ChoiceBox<String> videoCodec;
	private TextField videoFile;
	
	private Button modButton;
	private Button connect;
	private Button controls;
	private Button about;
	
	// Mod settings
	private TextField qualityHz;
	private TextField cpuCapHz;
	private CheckBox tgaHz;
	private Button applyHz;
	
	private TextField qualityCHM;
	private TextField cpuCapCHM;
	private ChoiceBox<String> reqScreenCHM;
	private CheckBox tgaCHM;
	private Button applyCHM;
	
	private TextField qualityNTR;
	private ChoiceBox<String> priScreen;
	private TextField priFac;
	private TextField qos;
	private Button ntrPatch;
	private Button applyNTR;
	
	private static final Logger logger = Logger.INSTANCE;
	
	public Scene setup(App app) throws IOException {
		
		// Initialize labels first
		Label modLab = new Label(Prop.MOD.getLongName());
		modLab.relocate(14, 14);
		Label ipLab = new Label(Prop.IP.getLongName());
		ipLab.relocate(14, 74);
		Label layoutLab = new Label(Prop.LAYOUT.getLongName());
		layoutLab.relocate(14, 104);
		Label topScaleLab = new Label(Prop.TOPSCALE.getLongName());
		topScaleLab.relocate(14, 134);
		Label bottomScaleLab = new Label(Prop.BOTTOMSCALE.getLongName());
		bottomScaleLab.relocate(14, 164);
		Label intrpModeLab = new Label(Prop.INTRPMODE.getLongName());
		intrpModeLab.relocate(14, 194);
		Label dpiLab = new Label(Prop.DPI.getLongName());
		dpiLab.relocate(12, 224);
		
		Label colorModeLab = new Label(Prop.COLORMODE.getLongName());
		colorModeLab.relocate(320, 14);
		Label portLab = new Label(Prop.PORT.getLongName());
		portLab.relocate(320, 44);
		Label logModeLab = new Label(Prop.LOGMODE.getLongName());
		logModeLab.relocate(320, 74);
		Label logLevelLab = new Label(Prop.LOGLEVEL.getLongName());
		logLevelLab.relocate(320, 104);
		Label logFileLab = new Label(Prop.LOGFILE.getLongName());
		logFileLab.relocate(320, 134);
		Label outputFormatLab = new Label(Prop.OUTPUTFORMAT.getLongName());
		outputFormatLab.relocate(320, 164);
		Label videoCodecLab = new Label(Prop.VIDEOCODEC.getLongName());
		videoCodecLab.relocate(320, 194);
		Label videoFileLab = new Label(Prop.VIDEOFILE.getLongName());
		videoFileLab.relocate(320, 224);
		
		// Add ChoiceBoxes next
		mod = new ChoiceBox<>();
		mod.getItems().addAll(getEnumNames(Mod.class));
		mod.relocate(150, 10);
		mod.setPrefWidth(150);
		layout = new ChoiceBox<>();
		layout.getItems().addAll(getEnumNames(Layout.class));
		layout.relocate(150, 100);
		layout.setPrefWidth(150);
		intrpMode = new ChoiceBox<>();
		intrpMode.getItems().addAll(getEnumNames(InterpolationMode.class));
		intrpMode.relocate(150, 190);
		intrpMode.setPrefWidth(150);
		colorMode = new ChoiceBox<>();
		colorMode.getItems().addAll(getEnumNames(ColorMode.class));
		colorMode.relocate(456, 10);
		colorMode.setPrefWidth(150);
		logMode = new ChoiceBox<>();
		logMode.getItems().addAll(getEnumNames(LogMode.class));
		logMode.relocate(456, 70);
		logMode.setPrefWidth(150);
		logMode.setOnAction((e) -> {
			logger.setMode(getPropEnum(Prop.LOGMODE, LogMode.class));
		});
		logLevel = new ChoiceBox<>();
		logLevel.getItems().addAll(getEnumNames(LogLevel.class));
		logLevel.relocate(456, 100);
		logLevel.setPrefWidth(150);
		logLevel.setOnAction((e) -> {
			logger.setLevel(getPropEnum(Prop.LOGLEVEL, LogLevel.class));
		});
		outputFormat = new ChoiceBox<>();
		outputFormat.getItems().addAll(getEnumNames(OutputFormat.class));
		outputFormat.relocate(456, 160);
		outputFormat.setPrefWidth(150);
		videoCodec = new ChoiceBox<>();
		videoCodec.getItems().addAll(getEnumNames(VideoFormat.class));
		videoCodec.relocate(456, 190);
		videoCodec.setPrefWidth(150);
		
		// Now onto TextFields
		ip = new TextField();
		ip.relocate(150, 70);
		topScale = new TextField();
		topScale.relocate(150, 130);
		topScale.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
		bottomScale = new TextField();
		bottomScale.relocate(150, 160);
		bottomScale.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
		dpi = new TextField();
		dpi.relocate(150, 220);
		dpi.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		port = new TextField();
		port.relocate(456, 40);
		port.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		logFile = new TextField();
		logFile.relocate(456, 130);
		videoFile = new TextField();
		videoFile.relocate(456, 220);
		
		// Finally, set up buttons
		modButton = new Button("Mod Settings");
		modButton.relocate(75, 40);
		modButton.setPrefWidth(150);
		connect = new Button("Connect!");
		connect.relocate(14, 250);
		connect.setPrefWidth(286);
		controls = new Button("Controls");
		controls.relocate(320, 250);
		controls.setPrefWidth(130);
		about = new Button("About");
		about.relocate(456, 250);
		about.setPrefWidth(150);
		
		// Pretty line
		Line line = new Line(0, 0, 0, 260);
		line.setLayoutX(310);
		line.setLayoutY(14);
		line.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.gray(.8)), new Stop(.5, Color.gray(.65)), new Stop(1, Color.gray(.8))));
		
		// Add handlers
		mod.setOnAction((e) -> {
			port.setText(
				switch(EnumProp.fromLongName(Mod.class, mod.getValue())) {
					case NTR -> "8000";
					case HZMOD, CHOKIMOD -> "6464";
				}
			);
		});
		outputFormat.setOnAction((e) -> {
			boolean isVisual = outputFormat.getValue().equals(OutputFormat.VISUAL.getLongName());
			videoCodec.setDisable(isVisual);
			videoFile.setDisable(isVisual);
		});
		about.setOnAction((e) -> {
			displayAbout();
		});
		connect.setOnAction((e) -> {
    		saveSettings();
    		app.connect();
    	});
		
		modButton.setOnAction((e) -> {
			switch(EnumProp.fromLongName(Mod.class, mod.getValue())) {
				case NTR:
					displayNTR();
					break;
				case HZMOD:
					displayHzMod();
					break;
				case CHOKIMOD:
					displayCHokiMod();
					break;
			}
		});
		
		// Set up minimal HzMod
		qualityHz = new TextField();
		qualityHz.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		cpuCapHz = new TextField();
		cpuCapHz.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		tgaHz = new CheckBox();
		tgaHz.setOnAction((e) -> {
			qualityHz.setText(tgaHz.isSelected() ? "0" : ""+Prop.QUALITY.getDefault());
		});
		qualityHz.setOnKeyTyped((e) -> {
			tgaHz.setSelected(qualityHz.getText().equals("0"));
		});
		
		// Set up minimal CHokiMod
		qualityCHM = new TextField();
		qualityCHM.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		cpuCapCHM = new TextField();
		cpuCapCHM.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		reqScreenCHM = new ChoiceBox<>();
		reqScreenCHM.getItems().addAll(getEnumNames(DSScreenBoth.class));
		tgaCHM = new CheckBox();
		tgaCHM.setOnAction((e) -> {
			qualityCHM.setDisable(tgaCHM.isSelected());
		});
		
		// Set up minimal NTR
		qualityNTR = new TextField();
		qualityNTR.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		priScreen = new ChoiceBox<>();
		priScreen.getItems().addAll(getEnumNames(DSScreen.class));
		priFac = new TextField();
		priFac.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		qos = new TextField();
		qos.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		
		populateFields();
		
		Pane pane = new Pane(modLab, mod, modButton, ipLab, ip, layoutLab, layout, topScaleLab, topScale, bottomScaleLab, bottomScale, dpiLab, dpi,
				intrpModeLab, intrpMode, connect, colorModeLab, colorMode, portLab, port, logModeLab, logMode, logLevelLab, logLevel,
				logFileLab, logFile, outputFormatLab, outputFormat, videoCodecLab, videoCodec, videoFileLab, videoFile, controls, about, line);
		return new Scene(pane, 620, 286);
	}
	
	private void displayAbout() {
		ImageView logo = new ImageView();
		logo.setImage(IconLoader.get64x());
		logo.relocate(10, 10);
		Text name = new Text(84, 64, "Chokistream");
		name.setFont(Font.font("System", FontWeight.BOLD, 60));
		
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
				+ " * toolboc for UWPStreamer\r\n"
				+ " * All other open-source contributors");
		t.setWrappingWidth(400);
		textScroll.setContent(t);
		t.relocate(7, 7); // Relative to textScroll
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
	
	private void displayNTR() {
		Label qualityNTRLab = new Label(Prop.QUALITY.getLongName());
		qualityNTRLab.relocate(14, 39);
		qualityNTR.relocate(150, 35);
		Label priScreenLab = new Label(Prop.PRIORITYSCREEN.getLongName());
		priScreenLab.relocate(14, 69);
		priScreen.relocate(150, 65);
		priScreen.setPrefWidth(150);
		Label priFacLab = new Label(Prop.PRIORITYFACTOR.getLongName());
		priFacLab.relocate(14, 99);
		priFac.relocate(150, 95);
		Label qosLab = new Label(Prop.QOS.getLongName());
		qosLab.relocate(14, 129);
		qos.relocate(150, 125);
		ntrPatch = new Button("Patch NTR");
		ntrPatch.relocate(82, 155);
		ntrPatch.setPrefWidth(150);
		applyNTR = new Button("Apply");
		applyNTR.relocate(14, 185);
		applyNTR.setPrefWidth(285);
		
		ntrPatch.setOnAction((e) -> {
    		Button n3ds = new Button(ConsoleModel.N3DS.getLongName());
    		Button o3ds = new Button(ConsoleModel.O3DS.getLongName());
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
					NTRClient.sendNFCPatch(getPropString(Prop.IP), getPropInt(Prop.PORT), null, ConsoleModel.N3DS);
				} catch (IOException | RuntimeException ex) {
					displayError(ex);
				}
    		});
    		o3ds.setOnAction((e2) -> {
    			try {
    				selectStage.close();
					NTRClient.sendNFCPatch(getPropString(Prop.IP), getPropInt(Prop.PORT), null, ConsoleModel.O3DS);
				} catch (IOException | RuntimeException ex) {
					displayError(ex);
				}
    		});
    		
    		selectStage.show();
    	});
		
		Text t = new Text("NTR Settings");
		t.relocate(14, 14);
		t.setFont(new Font(16));
		
		Pane pane = new Pane(t, qualityNTRLab, qualityNTR, priScreenLab, priScreen, priFacLab, priFac, qosLab, qos, ntrPatch, applyNTR);
		Scene sc = new Scene(pane, 312, 220);
		Stage st = new Stage();
		st.setScene(sc);
		st.setResizable(false);
		IconLoader.applyFavicon(st);
		
		applyNTR.setOnAction((e) -> {
    		saveSettings();
    		st.close();
    	});
		
		st.show();
	}
	
	private void displayHzMod() {
		Label qualityHzLab = new Label(Prop.QUALITY.getLongName());
		qualityHzLab.relocate(14, 39);
		qualityHz.relocate(150, 35);
		Label tgaHzLab = new Label("Request TGA?");
		tgaHzLab.relocate(14, 69);
		tgaHz.relocate(150, 69);
		Label cpuCapHzLab = new Label(Prop.CPUCAP.getLongName());
		cpuCapHzLab.relocate(14, 99);
		cpuCapHz.relocate(150, 95);
		applyHz = new Button("Apply");
		applyHz.relocate(14, 125);
		applyHz.setPrefWidth(285);
		Text t = new Text("HzMod Settings");
		t.relocate(14, 14);
		t.setFont(new Font(16));
		
		Pane pane = new Pane(t, qualityHzLab, qualityHz, tgaHzLab, tgaHz, cpuCapHzLab, cpuCapHz, applyHz);
		Scene sc = new Scene(pane, 312, 160);
		Stage st = new Stage();
		st.setScene(sc);
		st.setResizable(false);
		IconLoader.applyFavicon(st);
		
		applyHz.setOnAction((e) -> {
    		saveSettings();
    		st.close();
    	});
		
		st.show();
	}
	
	private void displayCHokiMod() {
		Label qualityCHMLab = new Label(Prop.QUALITY.getLongName());
		qualityCHMLab.relocate(14, 39);
		qualityCHM.relocate(150, 35);
		Label tgaCHMLab = new Label("Request TGA?");
		tgaCHMLab.relocate(14, 69);
		tgaCHM.relocate(150, 69);
		Label cpuCapCHMLab = new Label(Prop.CPUCAP.getLongName());
		cpuCapCHMLab.relocate(14, 99);
		cpuCapCHM.relocate(150, 95);
		Label reqScreenCHMLab = new Label(Prop.REQSCREEN.getLongName());
		reqScreenCHMLab.relocate(14, 129);
		reqScreenCHM.relocate(149, 125);
		reqScreenCHM.setPrefWidth(150);
		applyCHM = new Button("Apply");
		applyCHM.relocate(14, 155);
		applyCHM.setPrefWidth(285);
		Text t = new Text("CHokiMod Settings");
		t.relocate(14, 14);
		t.setFont(new Font(16));
		
		Pane pane = new Pane(t, qualityCHMLab, qualityCHM, tgaCHMLab, tgaCHM, cpuCapCHMLab, cpuCapCHM, reqScreenCHMLab, reqScreenCHM, applyCHM);
		Scene sc = new Scene(pane, 312, 190);
		Stage st = new Stage();
		st.setScene(sc);
		st.setResizable(false);
		IconLoader.applyFavicon(st);
		
		applyCHM.setOnAction((e) -> {
    		saveSettings();
    		st.close();
    	});
		
		st.show();
	}
	
	private static <T extends Enum<T> & EnumProp> String[] getEnumNames(Class<T> c) {
		T[] ecs = c.getEnumConstants();
		String[] names = new String[ecs.length];
		for(int i = 0; i < ecs.length; i++) {
			names[i] = ecs[i].getLongName();
		}
		return names;
	}
	
	@Override
	public void saveSettings() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			parser.setProp(Prop.IP, getPropString(Prop.IP));
			parser.setProp(Prop.MOD, getPropEnum(Prop.MOD, Mod.class));
			parser.setProp(Prop.PRIORITYSCREEN, getPropEnum(Prop.PRIORITYSCREEN, DSScreen.class));
			parser.setProp(Prop.PRIORITYFACTOR, getPropInt(Prop.PRIORITYFACTOR));
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
			parser.setProp(Prop.OUTPUTFORMAT, getPropEnum(Prop.OUTPUTFORMAT, OutputFormat.class));
			parser.setProp(Prop.VIDEOCODEC, getPropEnum(Prop.VIDEOCODEC, VideoFormat.class));
			parser.setProp(Prop.VIDEOFILE, getPropString(Prop.VIDEOFILE));
			parser.setProp(Prop.REQTGA, getPropBoolean(Prop.REQTGA));
			
			switch(getPropEnum(Prop.MOD, Mod.class)) {
				case NTR:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					break;
				case HZMOD:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					parser.setProp(Prop.CPUCAP, getPropInt(Prop.CPUCAP));
					parser.setProp(Prop.REQSCREEN, getPropEnum(Prop.REQSCREEN, DSScreenBoth.class));
					break;
				case CHOKIMOD:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					parser.setProp(Prop.CPUCAP, getPropInt(Prop.CPUCAP));
					parser.setProp(Prop.REQSCREEN, getPropEnum(Prop.REQSCREEN, DSScreenBoth.class));
					break;
			}
		} catch (IOException | IniParseException e) {
			displayError(e);
		}
	}
	
	private void populateFields() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			
			setTextDefault(parser, Prop.IP, ip);
			setTextDefault(parser, Prop.TOPSCALE, topScale);
			setTextDefault(parser, Prop.BOTTOMSCALE, bottomScale);
			setTextDefault(parser, Prop.PORT, port);
			setTextDefault(parser, Prop.LOGFILE, logFile);
			setTextDefault(parser, Prop.VIDEOFILE, videoFile);
			setTextDefault(parser, Prop.DPI, dpi);
			
			setValueDefault(parser, Prop.MOD, mod);
			setValueDefault(parser, Prop.LAYOUT, layout);
			setValueDefault(parser, Prop.INTRPMODE, intrpMode);
			setValueDefault(parser, Prop.COLORMODE, colorMode);
			setValueDefault(parser, Prop.LOGMODE, logMode);
			setValueDefault(parser, Prop.LOGLEVEL, logLevel);
			setValueDefault(parser, Prop.OUTPUTFORMAT, outputFormat);
			setValueDefault(parser, Prop.VIDEOCODEC, videoCodec);
			
			setTextDefault(parser, Prop.QUALITY, qualityHz);
			tgaHz.setSelected(qualityHz.getText().equals("0"));
			setTextDefault(parser, Prop.CPUCAP, cpuCapHz);
			
			setTextDefault(parser, Prop.QUALITY, qualityCHM);
			setCheckedDefault(parser, Prop.REQTGA, tgaCHM);
			qualityCHM.setDisable(tgaCHM.isSelected());
			setTextDefault(parser, Prop.CPUCAP, cpuCapCHM);
			setValueDefault(parser, Prop.REQSCREEN, reqScreenCHM);
			
			setTextDefault(parser, Prop.QUALITY, qualityNTR);
			setValueDefault(parser, Prop.PRIORITYSCREEN, priScreen);
			setTextDefault(parser, Prop.PRIORITYFACTOR, priFac);
			setTextDefault(parser, Prop.QOS, qos);
		} catch (IOException | IniParseException e) {
			displayError(e);
		}
	}
	
	private static void setTextDefault(INIParser parser, Prop<?> p, TextField tf) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			tf.setText(val);
		} else {
			tf.setText(p.getDefault().toString());
		}
	}
	
	private static <T extends EnumProp> void setValueDefault(INIParser parser, Prop<T> p, ChoiceBox<String> tf) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			tf.setValue(val);
		} else {
			tf.setValue(p.getDefault().getLongName());
		}
	}
	
	private static void setCheckedDefault(INIParser parser, Prop<Boolean> p, CheckBox cb) {
		String val = parser.getProp(p);
		if(val.length() > 0 && (val.equals("true") || val.equals("false"))) {
			cb.setSelected(val.equals("true"));
		} else {
			cb.setSelected(p.getDefault());
		}
	}
	
	// Generic popup
	@Override
	public void displayError(Exception e) {
		Stage popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Label message = new Label(sw.toString());
		message.setPadding(new Insets(7));
		Scene scene = new Scene(message);
		popup.setScene(scene);
		popup.setTitle("Error");
		IconLoader.applyFavicon(popup);
		popup.show();
	}
	
	@Override
	public int getPropInt(Prop<Integer> p) {
		if(p.equals(Prop.QUALITY)) {
			return switch(EnumProp.fromLongName(Mod.class, mod.getValue())) {
				case NTR -> Integer.parseInt(qualityNTR.getText());
				case HZMOD -> Integer.parseInt(qualityHz.getText());
				case CHOKIMOD -> Integer.parseInt(qualityCHM.getText());
			};
		} else if(p.equals(Prop.PRIORITYFACTOR)) {
			return Integer.parseInt(priFac.getText());
		} else if(p.equals(Prop.QOS)) {
			return Integer.parseInt(qos.getText());
		} else if(p.equals(Prop.CPUCAP)) {
			return switch(EnumProp.fromLongName(Mod.class, mod.getValue())) {
				case NTR -> p.getDefault(); // Hopefully never happens
				case HZMOD -> Integer.parseInt(cpuCapHz.getText());
				case CHOKIMOD -> Integer.parseInt(cpuCapCHM.getText());
			};
		} else if(p.equals(Prop.PORT)) {
			return Integer.parseInt(port.getText());
		} else if(p.equals(Prop.DPI)) {
			return Integer.parseInt(dpi.getText());
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public String getPropString(Prop<String> p) {
		if(p.equals(Prop.IP)) {
			return ip.getText();
		} else if(p.equals(Prop.LOGFILE)) {
			return logFile.getText();
		} else if(p.equals(Prop.VIDEOFILE)) {
			return videoFile.getText();
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public double getPropDouble(Prop<Double> p) {
		if(p.equals(Prop.TOPSCALE)) {
			return Double.parseDouble(topScale.getText());
		} else if(p.equals(Prop.BOTTOMSCALE)) {
			return Double.parseDouble(bottomScale.getText());
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public boolean getPropBoolean(Prop<Boolean> p) {
		if(p.equals(Prop.REQTGA)) {
			return tgaCHM.isSelected(); // Only used for CHokiMod
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p, Class<T> c) {
		if(p.equals(Prop.MOD)) {
			return EnumProp.fromLongName(c, mod.getValue());
		} else if(p.equals(Prop.LAYOUT)) {
			return EnumProp.fromLongName(c, layout.getValue());
		} else if(p.equals(Prop.PRIORITYSCREEN)) {
			return EnumProp.fromLongName(c, priScreen.getValue());
		} else if(p.equals(Prop.REQSCREEN)) {
			return switch(EnumProp.fromLongName(Mod.class, mod.getValue())) {
				case NTR -> p.getDefault(); // Hopefully never happens
				case HZMOD -> p.getDefault(); // Hopefully never happens
				case CHOKIMOD -> EnumProp.fromLongName(c, reqScreenCHM.getValue());
			};
		} else if(p.equals(Prop.COLORMODE)) {
			return EnumProp.fromLongName(c, colorMode.getValue());
		} else if(p.equals(Prop.LOGMODE)) {
			return EnumProp.fromLongName(c, logMode.getValue());
		} else if(p.equals(Prop.LOGLEVEL)) {
			return EnumProp.fromLongName(c, logLevel.getValue());
		} else if(p.equals(Prop.INTRPMODE)) {
			return EnumProp.fromLongName(c, intrpMode.getValue());
		} else if(p.equals(Prop.OUTPUTFORMAT)) {
			return EnumProp.fromLongName(c, outputFormat.getValue());
		} else if(p.equals(Prop.VIDEOCODEC)) {
			return EnumProp.fromLongName(c, videoCodec.getValue());
		} else {
			return p.getDefault();
		}
	}
}