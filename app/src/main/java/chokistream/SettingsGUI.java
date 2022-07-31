package chokistream;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsGUI extends Scene {
	
	public SettingsGUI(Parent p, double width, double height) {
		super(p, width, height);
	}
	
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
