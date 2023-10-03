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
import java.util.EnumMap;

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
import chokistream.Input.InputParseException;
import chokistream.props.ColorMode;
import chokistream.props.Controls;
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
	
	// Video settings
	private JFrame videoSettings;
	
	// Image sequence settings
	private JFrame sequenceSettings;
	
	// HzMod settings
	private JFrame hzSettings;
	private JTextField cpuCapHz;
	private JCheckBox tgaHz;
	
	// ChirunoMod settings
	private JFrame chmSettings;
	private JTextField cpuCapCHM;
	private JCheckBox tgaCHM;
	
	// NTR settings
	private JFrame ntrSettings;
	
	// NFC Patch screen
	private JFrame nfcPatch;
	
	// Controls
	private JFrame controls;
	private EnumMap<Controls, JTextField> controlsFields = new EnumMap<>(Controls.class);
	
	@SuppressWarnings("rawtypes")
	private JComboBox[] enumFields = new JComboBox[Prop.getCount()];
	private JTextField[] textFields = new JTextField[Prop.getCount()];
	private JCheckBox[] boolFields = new JCheckBox[Prop.getCount()];
	
	private static final Logger logger = Logger.INSTANCE;
	
	private static final String ABOUT_TEXT = "<html>Made by Eiim, herronjo, and ChainSwordCS.<br>"
								+ "<br>"
								+ "This software and its source code are licensed under GPLv2 or later, unless otherwise mentioned. NTRClient.java is licensed under GPLv2-only. See LICENSE for the full license.<br>"
								+ "<br>"
								+ "Chokistream was made possible by the use and reference of several projects. Special thanks to:<br>"
								+ " * RattletraPM for Snickerstream<br>"
								+ " * Sono for HzMod<br>"
								+ " * Cell9/44670 for BootNTR and NTRClient<br>"
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
		
		addOpts(Prop.MOD, p, c, 0, 0, Mod.class);
		addText(Prop.IP, p, c, 0, 2);
		addOpts(Prop.LAYOUT, p, c, 0, 3, Layout.class);
		addText(Prop.TOPSCALE, p, c, 0, 4);
		addText(Prop.BOTTOMSCALE, p, c, 0, 5);
		
		add(new JSeparator(SwingConstants.VERTICAL), p, c, 2, 0, 1, 8);
		
		addOpts(Prop.COLORMODE, p, c, 3, 0, ColorMode.class);
		addText(Prop.PORT, p, c, 3, 1);
		addOpts(Prop.LOGMODE, p, c, 3, 2, LogMode.class);
		addOpts(Prop.LOGLEVEL, p, c, 3, 3, LogLevel.class);
		addText(Prop.LOGFILE, p, c, 3, 4);
		addOpts(Prop.OUTPUTFORMAT, p, c, 3, 5, OutputFormat.class);
		
		JButton modSettings = new JButton("Mod Settings");
		JButton outputSettings = new JButton("Output Settings");
		JButton controlsButton = new JButton("Controls");
		JButton about = new JButton("About");
		JButton connect = new JButton("Connect!");
		add(modSettings, p, c, 0, 1, 2, 1);
		add(connect, p, c, 0, 7, 2, 1);
		add(outputSettings, p, c, 3, 6, 2, 1);
		add(controlsButton, p, c, 0, 6, 2, 1);
		add(about, p, c, 3, 7, 2, 1);
		
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {createAbout();}
		});

		controlsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {controls.setVisible(true);}
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
		
		enumFields[Prop.OUTPUTFORMAT.getIndex()].addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				outputSettings.setEnabled(!enumFields[Prop.OUTPUTFORMAT.getIndex()].getSelectedItem().equals(OutputFormat.VISUAL.getLongName()));
			}
		});
		
		enumFields[Prop.MOD.getIndex()].addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				textFields[Prop.PORT.getIndex()].setText(
					switch(getPropEnum(Prop.MOD)) {
						case NTR -> "8000";
						case HZMOD, CHIRUNOMOD -> "6464";
					}
				);
			}
		});
		
		enumFields[Prop.LOGLEVEL.getIndex()].addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.setLevel(getPropEnum(Prop.LOGLEVEL));
			}
		});
		
		enumFields[Prop.LOGMODE.getIndex()].addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.setMode(getPropEnum(Prop.LOGMODE));
			}
		});
		
		textFields[Prop.LOGFILE.getIndex()].getDocument().addDocumentListener(new DocumentListener() {
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
				saveControls();
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
		createControls();
		
		loadSettings();
		loadControls();
	}
	
	@Override
	public int getPropInt(Prop<Integer> p) {
		if(p.equals(Prop.CPUCAP)) {
			return switch(getPropEnum(Prop.MOD)) {
				case HZMOD -> Integer.parseInt(cpuCapHz.getText());
				case CHIRUNOMOD -> Integer.parseInt(cpuCapCHM.getText());
				default -> p.getDefault(); // Hopefully never happens
			};
		} else if(p.equals(Prop.QUALITY)) {
			return switch(getPropEnum(Prop.MOD)) {
				case NTR -> Integer.parseInt(textFields[p.getIndex()].getText());
				case HZMOD -> Integer.parseInt(textFields[p.getIndex()].getText());
				case CHIRUNOMOD -> Integer.parseInt(textFields[p.getIndex()].getText());
			};
		} else {
			JTextField tf = textFields[p.getIndex()];
			if(tf != null) {
				return Integer.parseInt(tf.getText());
			} else {
				return p.getDefault();	
			}
		}
	}

	@Override
	public String getPropString(Prop<String> p) {
		JTextField tf = textFields[p.getIndex()];
		if(tf != null) {
			return tf.getText();
		} else {
			return p.getDefault();
		}
	}

	@Override
	public double getPropDouble(Prop<Double> p) {
		JTextField tf = textFields[p.getIndex()];
		if(tf != null) {
			return Double.parseDouble(tf.getText());
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
			return boolFields[p.getIndex()].isSelected();
		} else {
			return p.getDefault();
		}
	}

	@Override
	public <T extends Enum<T> & EnumProp> T getPropEnum(Prop<T> p) {
		var ef = enumFields[p.getIndex()];
		if(ef != null) {
			return EnumProp.fromLongName(p.propClass(), ef.getSelectedItem().toString());
		} else {
			return p.getDefault();
		}
	}
	
	@Override
	public ChokiKeybinds getKeybinds() {
		ChokiKeybinds ck = ChokiKeybinds.getDefaults();
		for(Controls c : controlsFields.keySet())  {
			try {
				ck.set(c, new Input(controlsFields.get(c).getText()));
			} catch (InputParseException e) {
				displayError(e); // Continue onwards
			}
		}
		return ck;
	}

	@Override
	public void displayError(Exception e) {
		JOptionPane.showMessageDialog(f, e, "Error", JOptionPane.ERROR_MESSAGE);
	}

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
		} catch (IOException | IniParseException | NumberFormatException e) {
			displayError(e);
		}
	}

	public void loadSettings() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			
			setTextDefault(parser, Prop.IP);
			setTextDefault(parser, Prop.TOPSCALE);
			setTextDefault(parser, Prop.BOTTOMSCALE);
			setTextDefault(parser, Prop.PORT);
			setTextDefault(parser, Prop.LOGFILE);
			setTextDefault(parser, Prop.VIDEOFILE);
			
			setValueDefault(parser, Prop.MOD);
			setValueDefault(parser, Prop.LAYOUT);
			setValueDefault(parser, Prop.COLORMODE);
			setValueDefault(parser, Prop.LOGMODE);
			setValueDefault(parser, Prop.LOGLEVEL);
			
			setTextDefault(parser, Prop.QUALITY);
			tgaHz.setSelected(textFields[Prop.QUALITY.getIndex()].getText().equals("0"));
			setTextDefault(parser, Prop.CPUCAP, cpuCapHz);
			
			setTextDefault(parser, Prop.QUALITY);
			setCheckedDefault(parser, Prop.REQTGA, tgaCHM);
			setTextDefault(parser, Prop.CPUCAP, cpuCapCHM);
			setValueDefault(parser, Prop.REQSCREEN);
			setCheckedDefault(parser, Prop.INTERLACE);
			
			setTextDefault(parser, Prop.QUALITY);
			setValueDefault(parser, Prop.PRIORITYSCREEN);
			setTextDefault(parser, Prop.PRIORITYFACTOR);
			setTextDefault(parser, Prop.QOS);
			
			setValueDefault(parser, Prop.OUTPUTFORMAT);
			setValueDefault(parser, Prop.VIDEOCODEC);
			
			setTextDefault(parser, Prop.SEQUENCEDIR);
			setTextDefault(parser, Prop.SEQUENCEPREFIX);
		} catch (IOException | IniParseException e) {
			displayError(e);
		}
	}
	
	public void saveControls() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			
			for(Controls c : controlsFields.keySet()) {
				try {
					String text = controlsFields.get(c).getText();
					if(text.length() > 0) parser.setProp(c, text);
				} catch(InputParseException e) {
					displayError(e); // still continue onwards
				}
			}
		} catch(IOException | IniParseException e) {
			displayError(e); // these probably indicate the future ones will fail as well, so stop here
		}
	}

	public void loadControls() {
		try {
			INIParser parser = new INIParser(new File("chokistream.ini"));
			
			for(Controls c : controlsFields.keySet()) {
				setControlDefault(parser, c);
			}
		} catch(IOException | IniParseException e) {
			displayError(e); // these probably indicate the future ones will fail as well, so stop here
		}
	}

	private void setControlDefault(INIParser parser, Controls c) {
		String val = parser.getProp(c);
		JTextField tf = controlsFields.get(c);
		if(tf == null) return;
		if(val.length() > 0) {
			try {
				tf.setText(new Input(val).toString());
				return;
			} catch(InputParseException e) {
				logger.log("InputParseException: "+e.message, LogLevel.REGULAR);
			}
		}
		tf.setText(c.getDefault().toString()); // If missing or invalid, use default
	}
	
	private static void setTextDefault(INIParser parser, Prop<?> p, JTextField tf) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			tf.setText(val);
		} else {
			tf.setText(p.getDefault().toString());
		}
	}
	
	private void setTextDefault(INIParser parser, Prop<?> p) {
		String val = parser.getProp(p);
		if(val.length() > 0) {
			textFields[p.getIndex()].setText(val);
		} else {
			textFields[p.getIndex()].setText(p.getDefault().toString());
		}
	}
	
	private <T extends Enum<T> & EnumProp> void setValueDefault(INIParser parser, Prop<T> p) {
		JComboBox<?> ef = enumFields[p.getIndex()];
		if(ef == null) return;
		String val = parser.getProp(p);
		if(val.length() > 0) {
			enumFields[p.getIndex()].setSelectedItem(val);
		} else {
			enumFields[p.getIndex()].setSelectedItem(p.getDefault().getLongName());
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
	
	private void setCheckedDefault(INIParser parser, Prop<Boolean> p) {
		String val = parser.getProp(p);
		if(val.length() > 0 && (val.equals("true") || val.equals("false"))) {
			boolFields[p.getIndex()].setSelected(val.equals("true"));
		} else {
			boolFields[p.getIndex()].setSelected(p.getDefault());
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
		
		add(new JLabel(Controls.SCREENSHOT.getLongName()), p, c, 0, 1);
		add(new JLabel(Controls.RETURN.getLongName()), p, c, 0, 2);
		add(new JLabel(Controls.QUALITY_UP.getLongName()), p, c, 0, 3);
		add(new JLabel(Controls.QUALITY_DOWN.getLongName()), p, c, 0, 4);
		
		// Temporary, for layout
		controlsFields.put(Controls.SCREENSHOT, add(new JTextField(), p, c, 1, 1));
		controlsFields.put(Controls.RETURN, add(new JTextField(), p, c, 1, 2));
		controlsFields.put(Controls.QUALITY_UP, add(new JTextField(), p, c, 1, 3));
		controlsFields.put(Controls.QUALITY_DOWN, add(new JTextField(), p, c, 1, 4));
		
		JButton apply = new JButton("Apply");
		add(apply, p, c, 0, 5, 2, 1);
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
		
		addOpts(Prop.VIDEOCODEC, p, c, 0, 1, VideoFormat.class);
		addText(Prop.VIDEOFILE, p, c, 0, 2);
		
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
		
		addText(Prop.SEQUENCEDIR, p, c, 0, 1);
		addText(Prop.SEQUENCEPREFIX, p, c, 0, 2);
		
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
		
		addText(Prop.QUALITY, p, c, 0, 1);
		addOpts(Prop.PRIORITYSCREEN, p, c, 0, 2, DSScreen.class);
		addText(Prop.PRIORITYFACTOR, p, c, 0, 3);
		addText(Prop.QOS, p, c, 0, 4);
		
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
		
		addText(Prop.QUALITY, p, c, 0, 1);
		add(new JLabel(Prop.REQTGA.getLongName()), p, c, 0, 2);
		add(new JLabel(Prop.CPUCAP.getLongName()), p, c, 0, 3);
		
		tgaHz = new JCheckBox();
		add(tgaHz, p, c, 1, 2, "Request TARGA (lossless) image format.");
		cpuCapHz = new JTextField();
		add(cpuCapHz, p, c, 1, 3, "CPU usage limiter");
		
		tgaHz.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				textFields[Prop.QUALITY.getIndex()].setEnabled(!tgaHz.isSelected());
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
		
		addText(Prop.QUALITY, p, c, 0, 1);
		
		add(new JLabel("Request TGA?"), p, c, 0, 2);
		add(new JLabel("CPU Cap"), p, c, 0, 3);
		
		tgaCHM = new JCheckBox();
		add(tgaCHM, p, c, 1, 2, "Request TARGA (lossless) image format.");
		cpuCapCHM = new JTextField();
		add(cpuCapCHM, p, c, 1, 3, "CPU usage limiter");
		
		addOpts(Prop.REQSCREEN, p, c, 0, 4, DSScreenBoth.class);
		addCheck(Prop.INTERLACE, p, c, 0, 5);
		
		tgaCHM.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				textFields[Prop.QUALITY.getIndex()].setEnabled(!tgaCHM.isSelected());
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
	
	private void addText(Prop<?> p, JPanel f, GridBagConstraints c, int x, int y) {
		var tf = textFields[p.getIndex()];
		if(tf == null) tf = new JTextField();
		tf.setToolTipText(p.getTooltip());
		c.gridx = x;
		c.gridy = y;
		f.add(tf, c);
		
		var lab = new JLabel(p.getLongName());
		lab.setToolTipText(p.getTooltip());
		c.gridx = x+1;
		c.gridy = y;
		f.add(lab, c);
		
		textFields[p.getIndex()] = tf;
	}
	
	private <T extends Enum<T> & EnumProp> void addOpts(Prop<T> p, JPanel f, GridBagConstraints c, int x, int y, Class<T> cl) {
		var cb = enumFields[p.getIndex()];
		if(cb == null) cb = new JComboBox<String>(EnumProp.getLongNames(cl));
		cb.setToolTipText(p.getTooltip());
		c.gridx = x;
		c.gridy = y;
		f.add(cb, c);
		
		var lab = new JLabel(p.getLongName());
		lab.setToolTipText(p.getTooltip());
		c.gridx = x+1;
		c.gridy = y;
		f.add(lab, c);
		
		enumFields[p.getIndex()] = cb;
	}
	
	private void addCheck(Prop<Boolean> p, JPanel f, GridBagConstraints c, int x, int y) {
		var cb = boolFields[p.getIndex()];
		if(cb == null) cb = new JCheckBox();
		cb.setToolTipText(p.getTooltip());
		c.gridx = x;
		c.gridy = y;
		f.add(cb, c);
		
		var lab = new JLabel(p.getLongName());
		lab.setToolTipText(p.getTooltip());
		c.gridx = x+1;
		c.gridy = y;
		f.add(lab, c);
		
		boolFields[p.getIndex()] = cb;
	}
	
	// Return co for chaining
	private <T extends Component> T add(T co, JPanel f, GridBagConstraints c, int x, int y) {
		c.gridx = x;
		c.gridy = y;
		f.add(co, c);
		return co;
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
