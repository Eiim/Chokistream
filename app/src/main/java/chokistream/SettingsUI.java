package chokistream;

import java.awt.Toolkit;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsUI {
	
	// Use defaults from Snickerstream if the GUI doesn't implement them, could change in the future.
	protected String ip = "0.0.0.0";
	protected Mod mod = Mod.NTR;
	protected int quality = 70;
	protected NTRScreen screen = NTRScreen.TOP;
	protected int priority = 8;
	protected int qos = 26;
	protected int capCPU  = 0;
	protected Layout layout = Layout.SEPARATE;
	protected ColorMode colorMode = ColorMode.REGULAR;
	// Advanced settings defaults generally aren't based off Snickerstream
	protected int port = 8000;
	protected double topScale = 1;
	protected double bottomScale = 1;
	protected LogMode logMode = LogMode.CONSOLE;
	protected LogLevel logLevel = LogLevel.REGULAR;
	protected String logFile = "chokistream.log";
	protected InterpolationMode intrp = InterpolationMode.NONE;
	protected int custDPI = Toolkit.getDefaultToolkit().getScreenResolution();
	
	// These can throw exceptions in case the user inputs bad data (like "yummy" for the quality or something)
	
	/**
	 * @return The IP of the 3DS to connect to
	 * @throws InvalidOptionException
	 */
	public String getIp() throws InvalidOptionException {
		return ip;
	}
	
	/**
	 * @return The mod on the 3DS (NTR/HzMod)
	 * @throws InvalidOptionException
	 */
	public Mod getMod() throws InvalidOptionException {
		return mod;
	}
	
	/**
	 * @return The quality level for JPEG compression
	 * @throws InvalidOptionException
	 */
	public int getQuality() throws InvalidOptionException {
		return quality;
	}
	
	/**
	 * @return Whether the top or bottom screen should be prioritized
	 * @throws InvalidOptionException
	 */
	public NTRScreen getScreen() throws InvalidOptionException {
		return screen;
	}
	
	/**
	 * @return The degree to which the prioritized screen is prioritized
	 * @throws InvalidOptionException
	 */
	public int getPriority() throws InvalidOptionException {
		return priority;
	}
	
	
	/**
	 * @return Network packet priority setting
	 * @throws InvalidOptionException
	 */
	public int getQos() throws InvalidOptionException {
		return qos;
	}
	
	/**
	 * @return The CPU cap for NTR
	 * @throws InvalidOptionException
	 */
	public int getCapCPU() throws InvalidOptionException {
		return capCPU;
	}
	
	/**
	 * @return The layout of the output video
	 * @throws InvalidOptionException
	 */
	public Layout getLayout() throws InvalidOptionException {
		return layout;
	}
	
	/**
	 * @return The selected color adjustment mode
	 * @throws InvalidOptionException
	 */
	public ColorMode getColorMode() throws InvalidOptionException {
		return colorMode;
	}
	
	/**
	 * @return The selected port
	 * @throws InvalidOptionException
	 */
	public int getPort() throws InvalidOptionException {
		return port;
	}
	
	/**
	 * @return The scale of the top screen
	 * @throws InvalidOptionException
	 */
	public double getTopScale() throws InvalidOptionException {
		return topScale;
	}
	
	/**
	 * @return The Scale of the bottom screen
	 * @throws InvalidOptionException
	 */
	public double getBottomScale() throws InvalidOptionException {
		return bottomScale;
	}
	
	/**
	 * @return The logging output mode
	 * @throws InvalidOptionException
	 */
	public LogMode getLogMode() throws InvalidOptionException {
		return logMode;
	}
	
	/**
	 * @return The logging level
	 * @throws InvalidOptionException
	 */
	public LogLevel getLogLevel() throws InvalidOptionException {
		return logLevel;
	}
	
	/**
	 * @return The log file
	 * @throws InvalidOptionException
	 */
	public String getLogFile() throws InvalidOptionException {
		return logFile;
	}
	
	/**
	 * @return The interpolation mode for the stream display
	 * @throws InvalidOptionException
	 */
	public InterpolationMode getIntrpMode() throws InvalidOptionException {
		return intrp;
	}
	
	/**
	 * @return The DPI to use for scaling/DPI correction
	 * @throws InvalidOptionException
	 */
	public int getDPI() throws InvalidOptionException {
		return custDPI;
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
		popup.show();
	}
	
	public void saveSettings() {}
	
	public void loadSettings() {}
}
