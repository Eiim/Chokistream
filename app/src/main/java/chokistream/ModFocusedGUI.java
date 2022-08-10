package chokistream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import chokistream.INIParser.IniParseException;
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

public class ModFocusedGUI extends SettingsUI {
	
	private ChoiceBox<String> mod;
	private TextField ip;
	private ChoiceBox<String> layout;
	private TextField topScale;
	private TextField bottomScale;
	private ChoiceBox<String> intrpMode;
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
	private Button about;
	
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
		logLevel = new ChoiceBox<>();
		logLevel.getItems().addAll(getEnumNames(ColorMode.class));
		logLevel.relocate(456, 100);
		logLevel.setPrefWidth(150);
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
		connect.relocate(14, 220);
		connect.setPrefWidth(200);
		about = new Button("About");
		about.relocate(220, 220);
		about.setPrefWidth(80);
		
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
		
		populateFields();
		
		Pane pane = new Pane(modLab, mod, modButton, ipLab, ip, layoutLab, layout, topScaleLab, topScale, bottomScaleLab, bottomScale,
				intrpModeLab, intrpMode, connect, about, colorModeLab, colorMode, portLab, port, logModeLab, logMode, logLevelLab, logLevel,
				logFileLab, logFile, outputFormatLab, outputFormat, videoCodecLab, videoCodec, videoFileLab, videoFile);
		return new Scene(pane, 620, 256);
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
	
	private static <T extends Enum<T> & EnumProp> String[] getEnumNames(Class<T> c) {
		T[] ecs = c.getEnumConstants();
		String[] names = new String[ecs.length];
		for(int i = 0; i < ecs.length; i++) {
			names[i] = ecs[i].getLongName();
		}
		return names;
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
			
			setValueDefault(parser, Prop.MOD, mod);
			setValueDefault(parser, Prop.LAYOUT, layout);
			setValueDefault(parser, Prop.INTRPMODE, intrpMode);
			setValueDefault(parser, Prop.COLORMODE, colorMode);
			setValueDefault(parser, Prop.LOGMODE, logMode);
			setValueDefault(parser, Prop.LOGLEVEL, logLevel);
			setValueDefault(parser, Prop.OUTPUTFORMAT, outputFormat);
			setValueDefault(parser, Prop.VIDEOCODEC, videoCodec);
		} catch (FileNotFoundException | IniParseException e) {
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