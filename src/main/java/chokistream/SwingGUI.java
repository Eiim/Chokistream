package chokistream;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatLightLaf;

import chokistream.INIParser.IniParseException;
import chokistream.props.ColorMode;
import chokistream.props.DSScreen;
import chokistream.props.DSScreenBoth;
import chokistream.props.EnumProp;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;
import chokistream.props.Mod;
import chokistream.props.OutputFormat;
import chokistream.props.Prop;
import chokistream.props.VideoFormat;

public class SwingGUI extends SettingsUI {
	
	private JFrame f;
	
	// General settings
	private JComboBox<String> mod;
	private JTextField ip;
	private JComboBox<String> layout;
	private JTextField topScale;
	private JTextField bottomScale;
	private JComboBox<String> colorMode;
	private JTextField port;
	private JComboBox<String> logMode;
	private JComboBox<String> logLevel;
	private JTextField logFile;
	private JComboBox<String> outputFormat;
	
	// Video settings
	private JFrame videoSettings;
	private JComboBox<String> videoCodec;
	private JTextField videoFile;
	
	// Image sequence settings
	private JFrame sequenceSettings;
	private JTextField sequenceDir;
	private JTextField sequencePrefix;
	
	// HzMod settings
	private JFrame hzSettings;
	private JTextField qualityHz;
	private JTextField cpuCapHz;
	private JCheckBox tgaHz;
	
	// ChirunoMod settings
	private JFrame chmSettings;
	private JTextField qualityCHM;
	private JTextField cpuCapCHM;
	private JComboBox<String> reqScreenCHM;
	private JCheckBox tgaCHM;
	private JCheckBox interlace;
	
	// NTR settings
	private JFrame ntrSettings;
	private JTextField qualityNTR;
	private JComboBox<String> priScreen;
	private JTextField priFac;
	private JTextField qos;
	
	// NFC Patch screen
	private JFrame nfcPatch;
	
	// Controls
	private JFrame controls;
	
	private static final Logger logger = Logger.INSTANCE;
	
	private static final String ABOUT_TEXT = "<html>Made by Eiim, herronjo, and ChainSwordCS.<br>"
								+ "<br>"
								+ "This software and its source code are provided under the GPL-3.0 License.  See LICENSE for the full license.<br>"
								+ "<br>"
								+ "Chokistream was made possible by the use and reference of several projects. Special thanks to:<br>"
								+ " * RattletraPM for Snickerstream<br>"
								+ " * Sono for HzMod<br>"
								+ " * Cell9/44670 for BootNTR<br>"
								+ " * Nanquitas for BootNTRSelector<br>"
								+ " * toolboc for UWPStreamer<br>"
								+ " * All other open-source contributors";
	
	public SwingGUI() {
		FlatLightLaf.setup();
		f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setTitle("Chokistream");
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(f, p, c);
		
		add(new JLabel(Prop.MOD.getLongName()), p, c, 0, 0);
		add(new JLabel(Prop.IP.getLongName()), p, c, 0, 2);
		add(new JLabel(Prop.LAYOUT.getLongName()), p, c, 0, 3);
		add(new JLabel(Prop.TOPSCALE.getLongName()), p, c, 0, 4);
		add(new JLabel(Prop.BOTTOMSCALE.getLongName()), p, c, 0, 5);
		
		mod = new JComboBox<String>(EnumProp.getLongNames(Mod.class));
		add(mod, p, c, 1, 0, "3DS mod to connect to");
		ip = new JTextField();
		add(ip, p, c, 1, 2, "IP of the 3DS");
		layout = new JComboBox<String>(EnumProp.getLongNames(Layout.class));
		add(layout, p, c, 1, 3, "Layout of the screens. Choose Top Only unless using a dual-screen mod/version.");
		topScale = new JTextField();
		add(topScale, p, c, 1, 4, "Factor to scale the top screen by");
		bottomScale = new JTextField();
		add(bottomScale, p, c, 1, 5, "Factor to scale the bottom screen by");
		
		add(new JSeparator(SwingConstants.VERTICAL), p, c, 2, 0, 1, 8);
		
		add(new JLabel(Prop.COLORMODE.getLongName()), p, c, 3, 0);
		add(new JLabel(Prop.PORT.getLongName()), p, c, 3, 1);
		add(new JLabel(Prop.LOGMODE.getLongName()), p, c, 3, 2);
		add(new JLabel(Prop.LOGLEVEL.getLongName()), p, c, 3, 3);
		add(new JLabel(Prop.LOGFILE.getLongName()), p, c, 3, 4);
		add(new JLabel(Prop.OUTPUTFORMAT.getLongName()), p, c, 3, 5);
		
		colorMode = new JComboBox<String>(EnumProp.getLongNames(ColorMode.class));
		add(colorMode, p, c, 4, 0, "Color correction options");
		port = new JTextField();
		add(port, p, c, 4, 1, "3DS port, usually leave as default");
		logMode = new JComboBox<String>(EnumProp.getLongNames(LogMode.class));
		add(logMode, p, c, 4, 2, "Log to file or console");
		logLevel = new JComboBox<String>(EnumProp.getLongNames(LogLevel.class));
		add(logLevel, p, c, 4, 3, "Amount of detail in logs");
		logFile = new JTextField();
		add(logFile, p, c, 4, 4, "Filename for log file");
		outputFormat = new JComboBox<String>(EnumProp.getLongNames(OutputFormat.class));
		add(outputFormat, p, c, 4, 5, "Output format. Switch to file streaming or image sequence for file output.");
		
		JButton modSettings = new JButton("Mod Settings");
		JButton outputSettings = new JButton("Output Settings");
		JButton controls = new JButton("Controls");
		JButton about = new JButton("About");
		JButton connect = new JButton("Connect!");
		add(modSettings, p, c, 0, 1, 2, 1);
		add(connect, p, c, 0, 7, 2, 1);
		add(outputSettings, p, c, 3, 6, 2, 1);
		add(controls, p, c, 0, 6, 2, 1);
		add(about, p, c, 3, 7, 2, 1);
		
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {createAbout();}
		});
		
		modSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(getPropEnum(Prop.MOD)) {
					case HZMOD -> hzSettings.setVisible(true);
					case CHIRUNOMOD -> chmSettings.setVisible(true);
					case NTR -> ntrSettings.setVisible(true);
				}
			}
		});
		
		outputSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(getPropEnum(Prop.OUTPUTFORMAT)) {
					case FILE -> videoSettings.setVisible(true);
					case SEQUENCE -> sequenceSettings.setVisible(true);
					default -> {}
				}
			}
		});
		
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				OutputFormat outForm = getPropEnum(Prop.OUTPUTFORMAT);
		    	switch(outForm) {
		    		case VISUAL -> Main.initializeSwing(SwingGUI.this);
		    		case FILE -> Main.initializeFile(SwingGUI.this);
		    		case SEQUENCE -> Main.initializeSequence(SwingGUI.this);
		    	}
			}
		});
		
		outputFormat.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				outputSettings.setEnabled(!outputFormat.getSelectedItem().equals(OutputFormat.VISUAL.getLongName()));
			}
		});
		
		mod.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				port.setText(
					switch(getPropEnum(Prop.MOD)) {
						case NTR -> "8000";
						case HZMOD, CHIRUNOMOD -> "6464";
					}
				);
			}
		});
		
		logLevel.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.setLevel(getPropEnum(Prop.LOGLEVEL));
			}
		});
		
		logMode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.setMode(getPropEnum(Prop.LOGMODE));
			}
		});
		
		logFile.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				logger.setFile(getPropString(Prop.LOGFILE));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {changedUpdate(e);}

			@Override
			public void removeUpdate(DocumentEvent e) {changedUpdate(e);}
		});
		
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveSettings();
				System.exit(0);
			}
		});
		
		f.getRootPane().setDefaultButton(connect);
		f.pack();
		f.setVisible(true);
		
		createHzModSettings();
		createNTRSettings();
		createNFCPatch();
		createCHMSettings();
		createVideoSettings();
		createSequenceSettings();
		
		loadSettings();
	}
	
	@Override
	public int getPropInt(Prop<Integer> p) {
		if(p.equals(Prop.QUALITY)) {
			return switch(getPropEnum(Prop.MOD)) {
				case NTR -> Integer.parseInt(qualityNTR.getText());
				case HZMOD -> Integer.parseInt(qualityHz.getText());
				case CHIRUNOMOD -> Integer.parseInt(qualityCHM.getText());
			};
		} else if(p.equals(Prop.PRIORITYFACTOR)) {
			return Integer.parseInt(priFac.getText());
		} else if(p.equals(Prop.QOS)) {
			return Integer.parseInt(qos.getText());
		} else if(p.equals(Prop.CPUCAP)) {
			return switch(getPropEnum(Prop.MOD)) {
				case HZMOD -> Integer.parseInt(cpuCapHz.getText());
				case CHIRUNOMOD -> Integer.parseInt(cpuCapCHM.getText());
				default -> p.getDefault(); // Hopefully never happens
			};
		} else if(p.equals(Prop.PORT)) {
			return Integer.parseInt(port.getText());
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
		} else if(p.equals(Prop.SEQUENCEDIR)) {
			return sequenceDir.getText();
		} else if(p.equals(Prop.SEQUENCEPREFIX)) {
			return sequencePrefix.getText();
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
			return switch(getPropEnum(Prop.MOD)) {
				case CHIRUNOMOD ->  tgaCHM.isSelected();
				case HZMOD -> tgaHz.isSelected();
				default -> p.getDefault(); // Should never happen
			};
		} else if(p.equals(Prop.INTERLACE)) {
			return interlace.isSelected();
		} else {
			return p.getDefault();
		}
	}

	@Override
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p) {
		if(p.equals(Prop.MOD)) {
			return EnumProp.fromLongName(p.propClass(), mod.getSelectedItem().toString());
		} else if(p.equals(Prop.LAYOUT)) {
			return EnumProp.fromLongName(p.propClass(), layout.getSelectedItem().toString());
		} else if(p.equals(Prop.PRIORITYSCREEN)) {
			return EnumProp.fromLongName(p.propClass(), priScreen.getSelectedItem().toString());
		} else if(p.equals(Prop.REQSCREEN)) {
			return switch(getPropEnum(Prop.MOD)) {
				case CHIRUNOMOD -> EnumProp.fromLongName(p.propClass(), reqScreenCHM.getSelectedItem().toString());
				default -> p.getDefault(); // Hopefully never happens
			};
		} else if(p.equals(Prop.COLORMODE)) {
			return EnumProp.fromLongName(p.propClass(), colorMode.getSelectedItem().toString());
		} else if(p.equals(Prop.LOGMODE)) {
			return EnumProp.fromLongName(p.propClass(), logMode.getSelectedItem().toString());
		} else if(p.equals(Prop.LOGLEVEL)) {
			return EnumProp.fromLongName(p.propClass(), logLevel.getSelectedItem().toString());
		} else if(p.equals(Prop.OUTPUTFORMAT)) {
			return EnumProp.fromLongName(p.propClass(), outputFormat.getSelectedItem().toString());
		} else if(p.equals(Prop.VIDEOCODEC)) {
			return EnumProp.fromLongName(p.propClass(), videoCodec.getSelectedItem().toString());
		} else {
			return p.getDefault();
		}
	}
	
	/*
	 * Just use defaults for now
	 */
	public ChokiKeybinds getKeybinds() {
		return ChokiKeybinds.getDefaults();
	}

	@Override
	public void displayError(Exception e) {
		JOptionPane.showMessageDialog(f, e, "Error", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void saveSettings() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			parser.setProp(Prop.IP, getPropString(Prop.IP));
			parser.setProp(Prop.MOD, getPropEnum(Prop.MOD));
			parser.setProp(Prop.PRIORITYSCREEN, getPropEnum(Prop.PRIORITYSCREEN));
			parser.setProp(Prop.PRIORITYFACTOR, getPropInt(Prop.PRIORITYFACTOR));
			parser.setProp(Prop.LAYOUT, getPropEnum(Prop.LAYOUT));
			parser.setProp(Prop.COLORMODE, getPropEnum(Prop.COLORMODE));
			parser.setProp(Prop.PORT, getPropInt(Prop.PORT));
			parser.setProp(Prop.TOPSCALE, getPropDouble(Prop.TOPSCALE));
			parser.setProp(Prop.BOTTOMSCALE, getPropDouble(Prop.BOTTOMSCALE));
			parser.setProp(Prop.LOGMODE, getPropEnum(Prop.LOGMODE));
			parser.setProp(Prop.LOGLEVEL, getPropEnum(Prop.LOGLEVEL));
			parser.setProp(Prop.LOGFILE, getPropString(Prop.LOGFILE));
			parser.setProp(Prop.INTRPMODE, getPropEnum(Prop.INTRPMODE));
			parser.setProp(Prop.OUTPUTFORMAT, getPropEnum(Prop.OUTPUTFORMAT));
			parser.setProp(Prop.VIDEOCODEC, getPropEnum(Prop.VIDEOCODEC));
			parser.setProp(Prop.VIDEOFILE, getPropString(Prop.VIDEOFILE));
			parser.setProp(Prop.SEQUENCEDIR, getPropString(Prop.SEQUENCEDIR));
			parser.setProp(Prop.SEQUENCEPREFIX, getPropString(Prop.SEQUENCEPREFIX));
			parser.setProp(Prop.REQTGA, getPropBoolean(Prop.REQTGA));
			
			switch(getPropEnum(Prop.MOD)) {
				case NTR:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					parser.setProp(Prop.PRIORITYSCREEN, getPropEnum(Prop.PRIORITYSCREEN));
					parser.setProp(Prop.PRIORITYFACTOR, getPropInt(Prop.PRIORITYFACTOR));
					parser.setProp(Prop.QOS, getPropInt(Prop.QOS));
					break;
				case HZMOD:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					parser.setProp(Prop.CPUCAP, getPropInt(Prop.CPUCAP));
					break;
				case CHIRUNOMOD:
					parser.setProp(Prop.QUALITY, getPropInt(Prop.QUALITY));
					parser.setProp(Prop.CPUCAP, getPropInt(Prop.CPUCAP));
					parser.setProp(Prop.REQSCREEN, getPropEnum(Prop.REQSCREEN));
					parser.setProp(Prop.INTERLACE, getPropBoolean(Prop.INTERLACE));
					break;
			}
		} catch (IOException | IniParseException e) {
			displayError(e);
		}
	}

	@Override
	public void loadSettings() {
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
			setValueDefault(parser, Prop.COLORMODE, colorMode);
			setValueDefault(parser, Prop.LOGMODE, logMode);
			setValueDefault(parser, Prop.LOGLEVEL, logLevel);
			
			setTextDefault(parser, Prop.QUALITY, qualityHz);
			tgaHz.setSelected(qualityHz.getText().equals("0"));
			setTextDefault(parser, Prop.CPUCAP, cpuCapHz);
			
			setTextDefault(parser, Prop.QUALITY, qualityCHM);
			setCheckedDefault(parser, Prop.REQTGA, tgaCHM);
			setTextDefault(parser, Prop.CPUCAP, cpuCapCHM);
			setValueDefault(parser, Prop.REQSCREEN, reqScreenCHM);
			setCheckedDefault(parser, Prop.INTERLACE, interlace);
			
			setTextDefault(parser, Prop.QUALITY, qualityNTR);
			setValueDefault(parser, Prop.PRIORITYSCREEN, priScreen);
			setTextDefault(parser, Prop.PRIORITYFACTOR, priFac);
			setTextDefault(parser, Prop.QOS, qos);
			
			setValueDefault(parser, Prop.OUTPUTFORMAT, outputFormat);
			setValueDefault(parser, Prop.VIDEOCODEC, videoCodec);
			
			setTextDefault(parser, Prop.SEQUENCEDIR, sequenceDir);
			setTextDefault(parser, Prop.SEQUENCEPREFIX, sequencePrefix);
		} catch (IOException | IniParseException e) {
			displayError(e);
		}
	}
	
	public void saveControls() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			
		} catch(IOException | IniParseException e) {
			displayError(e);
		}
	}
	
	private static void setTextDefault(INIParser parser, Prop<?> p, JTextField tf) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			tf.setText(val);
		} else {
			tf.setText(p.getDefault().toString());
		}
	}
	
	private static <T extends EnumProp> void setValueDefault(INIParser parser, Prop<T> p, JComboBox<String> tf) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			tf.setSelectedItem(val);
		} else {
			tf.setSelectedItem(p.getDefault().getLongName());
		}
	}
	
	private static void setCheckedDefault(INIParser parser, Prop<Boolean> p, JCheckBox cb) {
		String val = parser.getProp(p);
		if(val.length() > 0 && (val.equals("true") || val.equals("false"))) {
			cb.setSelected(val.equals("true"));
		} else {
			cb.setSelected(p.getDefault());
		}
	}

	public void createAbout() {
		JFrame f = new JFrame();
		f.setResizable(false);
		f.setIconImages(IconLoader.getAll());
		f.setTitle("About");
		
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 7, 10));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		f.add(p);
		
		JLabel header = new JLabel("Chokistream", new LogoIcon(), JLabel.LEFT);
		header.setFont(new Font("System", Font.BOLD, 60));
		p.add(header);
		
		JLabel info = new JLabel(ABOUT_TEXT);
		p.add(info);
		
		f.pack();
		f.setVisible(true);
	}
	
	public void createControls() {
		controls = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(controls, p, c);
		controls.setTitle("Controls");
		
		JLabel header = new JLabel("Controls");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel(Prop.VIDEOCODEC.getLongName()), p, c, 0, 1);
		add(new JLabel(Prop.VIDEOFILE.getLongName()), p, c, 0, 2);
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 3, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveControls();
				controls.setVisible(false);
			}
		});
		
		controls.pack();
	}
	
	public void createVideoSettings() {
		videoSettings = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(videoSettings, p, c);
		videoSettings.setTitle("Video Settings");
		
		JLabel header = new JLabel("Video Settings");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel(Prop.VIDEOCODEC.getLongName()), p, c, 0, 1);
		add(new JLabel(Prop.VIDEOFILE.getLongName()), p, c, 0, 2);
		
		videoCodec = new JComboBox<String>(EnumProp.getLongNames(VideoFormat.class));;
		add(videoCodec, p, c, 1, 1, "Codec for video file output");
		videoFile = new JTextField();
		add(videoFile, p, c, 1, 2, "File name for video file output");
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 3, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				videoSettings.setVisible(false);
			}
		});
		
		videoSettings.pack();
	}
	
	public void createSequenceSettings() {
		sequenceSettings = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(sequenceSettings, p, c);
		sequenceSettings.setTitle("Sequence Settings");
		
		JLabel header = new JLabel("Sequence Settings");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel(Prop.SEQUENCEDIR.getLongName()), p, c, 0, 1);
		add(new JLabel(Prop.SEQUENCEPREFIX.getLongName()), p, c, 0, 2);
		
		sequenceDir = new JTextField();
		add(sequenceDir, p, c, 1, 1, "Directory for image sequences");
		sequencePrefix = new JTextField();
		add(sequencePrefix, p, c, 1, 2, "Prefix for image sequence files");
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 3, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				sequenceSettings.setVisible(false);
			}
		});
		
		sequenceSettings.pack();
	}
	
	public void createNTRSettings() {
		ntrSettings = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(ntrSettings, p, c);
		ntrSettings.setTitle("NTR Settings");
		
		JLabel header = new JLabel("NTR Settings");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel(Prop.QUALITY.getLongName()), p, c, 0, 1);
		add(new JLabel(Prop.PRIORITYSCREEN.getLongName()), p, c, 0, 2);
		add(new JLabel(Prop.PRIORITYFACTOR.getLongName()), p, c, 0, 3);
		add(new JLabel(Prop.QOS.getLongName()), p, c, 0, 4);
		
		qualityNTR = new JTextField();
		add(qualityNTR, p, c, 1, 1, "JPEG compression quality (0-100)");
		priScreen = new JComboBox<>(EnumProp.getLongNames(DSScreen.class));
		add(priScreen, p, c, 1, 2, "Prioritized screen");
		priFac = new JTextField();
		add(priFac, p, c, 1, 3, "Relative prioritization of prioritized screen");
		qos = new JTextField("Packet QoS value (Set to >100 to disable)");
		add(qos, p, c, 1, 4);
		
		JButton patch = new JButton("Patch NTR");
		add(patch, p, c, 0, 5, 2, 1);
		patch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nfcPatch.setVisible(true);
			}
		});
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 6, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				ntrSettings.setVisible(false);
			}
		});
		
		ntrSettings.pack();
	}
	
	public void createNFCPatch() {
		nfcPatch = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(nfcPatch, p, c);
		nfcPatch.setTitle("NFC Patch");
		
		JLabel header = new JLabel("NFC Patch");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		JButton latest = new JButton(">= 11.4");
		add(latest, p, c, 0, 1);
		latest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
    				nfcPatch.setVisible(false);
					NTRClient.sendNFCPatch(getPropString(Prop.IP), getPropInt(Prop.PORT), 1);
				} catch (RuntimeException ex) {
					displayError(ex);
				}
			}
		});
		
		JButton pre11_4 = new JButton("< 11.4");
		add(pre11_4, p, c, 1, 1);
		pre11_4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
    				nfcPatch.setVisible(false);
    				NTRClient.sendNFCPatch(getPropString(Prop.IP), getPropInt(Prop.PORT), 0);
				} catch (RuntimeException ex) {
					displayError(ex);
				}
			}
		});
		
		nfcPatch.pack();
	}
	
	public void createHzModSettings() {
		hzSettings = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(hzSettings, p, c);
		hzSettings.setTitle("HzMod Settings");
		
		JLabel header = new JLabel("HzMod Settings");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel(Prop.QUALITY.getLongName()), p, c, 0, 1);
		add(new JLabel(Prop.REQTGA.getLongName()), p, c, 0, 2);
		add(new JLabel(Prop.CPUCAP.getLongName()), p, c, 0, 3);
		
		qualityHz = new JTextField();
		add(qualityHz, p, c, 1, 1, "JPEG compression quality (1-100). Set to 0 to request TARGA.");
		tgaHz = new JCheckBox();
		add(tgaHz, p, c, 1, 2, "Request TARGA (lossless) image format.");
		cpuCapHz = new JTextField();
		add(cpuCapHz, p, c, 1, 3, "CPI usage limiter");
		
		tgaHz.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				qualityHz.setEnabled(!tgaHz.isSelected());
			}
		});
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 4, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				hzSettings.setVisible(false);
			}
		});
		
		hzSettings.pack();
	}
	
	public void createCHMSettings() {
		chmSettings = new JFrame();
		JPanel p = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		frameSetup(chmSettings, p, c);
		chmSettings.setTitle("ChirunoMod Settings");
		
		JLabel header = new JLabel("ChirunoMod Settings");
		header.setFont(new Font("System", Font.PLAIN, 20));
		add(header, p, c, 0, 0, 2, 1);
		
		add(new JLabel("Quality"), p, c, 0, 1);
		add(new JLabel("Request TGA?"), p, c, 0, 2);
		add(new JLabel("CPU Cap"), p, c, 0, 3);
		add(new JLabel("Requested Screen"), p, c, 0, 4);
		add(new JLabel("Interlace?"), p, c, 0, 5);
		
		qualityCHM = new JTextField();
		add(qualityCHM, p, c, 1, 1, "JPEG compression quality (1-100). Set to 0 to request TARGA.");
		tgaCHM = new JCheckBox();
		add(tgaCHM, p, c, 1, 2, "Request TARGA (lossless) image format.");
		cpuCapCHM = new JTextField();
		add(cpuCapCHM, p, c, 1, 3, "CPU usage limiter");
		reqScreenCHM = new JComboBox<>(EnumProp.getLongNames(DSScreenBoth.class));
		add(reqScreenCHM, p, c, 1, 4, "Requested 3DS screen");
		interlace = new JCheckBox();
		add(interlace, p, c, 1, 5, "Request image interlacing for higher apparent FPS.");
		
		tgaCHM.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				qualityCHM.setEnabled(!tgaCHM.isSelected());
			}
		});
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 6, 2, 1);
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				chmSettings.setVisible(false);
			}
		});
		
		chmSettings.pack();
	}
	
	private void frameSetup(JFrame f, JPanel p, GridBagConstraints c) {
		f.setResizable(false);
		f.setIconImages(IconLoader.getAll());
		
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 7, 10));
		p.setLayout(new GridBagLayout());
		f.add(p);
		
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 3;
		c.ipady = 3;
		c.insets = new Insets(3, 3, 3, 3);
	}
	
	private void add(Component co, JPanel f, GridBagConstraints c, int x, int y) {
		c.gridx = x;
		c.gridy = y;
		f.add(co, c);
	}
	
	private void add(JComponent co, JPanel f, GridBagConstraints c, int x, int y, String tooltip) {
		co.setToolTipText(tooltip);
		add(co, f, c, x, y);
	}
	
	private void add(Component co, JPanel f, GridBagConstraints c, int x, int y, int w, int h) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = w;
		c.gridheight = h;
		f.add(co, c);
		c.gridwidth = 1;
		c.gridheight = 1;
	}
}
