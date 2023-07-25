package chokistream;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatLightLaf;

import chokistream.props.ColorMode;
import chokistream.props.EnumProp;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;
import chokistream.props.Mod;
import chokistream.props.OutputFormat;
import chokistream.props.Prop;
import chokistream.props.VideoFormat;

public class SwingGUI extends SettingsUI {
	
	private JFrame f;
	
	private JComboBox<String> mod;
	private JTextField ip;
	private JComboBox<String> layout;
	private JTextField topScale;
	private JTextField bottomScale;
	private JComboBox<String> intrpMode;
	private JTextField dpi;
	private JComboBox<String> colorMode;
	private JTextField port;
	private JComboBox<String> logMode;
	private JComboBox<String> logLevel;
	private JTextField logFile;
	private JComboBox<String> outputFormat;
	private JComboBox<String> videoCodec;
	private JTextField videoFile;
	
	private static final Logger logger = Logger.INSTANCE;
	
	public SwingGUI() {
		FlatLightLaf.setup();
		f = new JFrame();
		f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setIconImage(IconLoader.get64x());
		f.setTitle("Chokistream");
		
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
		f.add(p);
		
		p.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 3;
		c.ipady = 3;
		c.insets = new Insets(3, 3, 3, 3);
		
		add(new JLabel(Prop.MOD.getLongName()), p, c, 0, 0);
		add(new JLabel(Prop.IP.getLongName()), p, c, 0, 2);
		add(new JLabel(Prop.LAYOUT.getLongName()), p, c, 0, 3);
		add(new JLabel(Prop.TOPSCALE.getLongName()), p, c, 0, 4);
		add(new JLabel(Prop.BOTTOMSCALE.getLongName()), p, c, 0, 5);
		add(new JLabel(Prop.INTRPMODE.getLongName()), p, c, 0, 6);
		add(new JLabel(Prop.DPI.getLongName()), p, c, 0, 7);
		
		mod = new JComboBox<String>(EnumProp.getLongNames(Mod.class));
		add(mod, p, c, 1, 0);
		ip = new JTextField(Prop.IP.getDefault());
		add(ip, p, c, 1, 2);
		layout = new JComboBox<String>(EnumProp.getLongNames(Layout.class));
		add(layout, p, c, 1, 3);
		topScale = new JTextField(Prop.TOPSCALE.getDefault().toString());
		add(topScale, p, c, 1, 4);
		bottomScale = new JTextField(Prop.BOTTOMSCALE.getDefault().toString());
		add(bottomScale, p, c, 1, 5);
		intrpMode = new JComboBox<String>(EnumProp.getLongNames(InterpolationMode.class));
		add(intrpMode, p, c, 1, 6);
		dpi = new JTextField(Prop.DPI.getDefault().toString());
		add(dpi, p, c, 1, 7);
		
		add(new JSeparator(SwingConstants.VERTICAL), p, c, 2, 0, 1, 8);
		
		add(new JLabel(Prop.COLORMODE.getLongName()), p, c, 3, 0);
		add(new JLabel(Prop.PORT.getLongName()), p, c, 3, 1);
		add(new JLabel(Prop.LOGMODE.getLongName()), p, c, 3, 2);
		add(new JLabel(Prop.LOGLEVEL.getLongName()), p, c, 3, 3);
		add(new JLabel(Prop.LOGFILE.getLongName()), p, c, 3, 4);
		add(new JLabel(Prop.OUTPUTFORMAT.getLongName()), p, c, 3, 5);
		add(new JLabel(Prop.VIDEOCODEC.getLongName()), p, c, 3, 6);
		add(new JLabel(Prop.VIDEOFILE.getLongName()), p, c, 3, 7);
		
		colorMode = new JComboBox<String>(EnumProp.getLongNames(ColorMode.class));
		add(colorMode, p, c, 4, 0);
		port = new JTextField(Prop.IP.getDefault());
		add(port, p, c, 4, 1);
		logMode = new JComboBox<String>(EnumProp.getLongNames(LogMode.class));
		add(logMode, p, c, 4, 2);
		logLevel = new JComboBox<String>(EnumProp.getLongNames(LogLevel.class));
		add(logLevel, p, c, 4, 3);
		logFile = new JTextField(Prop.LOGFILE.getDefault());
		add(logFile, p, c, 4, 4);
		outputFormat = new JComboBox<String>(EnumProp.getLongNames(OutputFormat.class));
		add(outputFormat, p, c, 4, 5);
		videoCodec = new JComboBox<String>(EnumProp.getLongNames(VideoFormat.class));
		add(videoCodec, p, c, 4, 6);
		videoFile = new JTextField(Prop.VIDEOFILE.getDefault());
		add(videoFile, p, c, 4, 7);
		
		add(new JButton("Mod Settings"), p, c, 0, 1, 2, 1);
		
		f.pack();
		f.setVisible(true);
	}
	
	private void add(Component co, JPanel f, GridBagConstraints c, int x, int y) {
		c.gridx = x;
		c.gridy = y;
		f.add(co, c);
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
