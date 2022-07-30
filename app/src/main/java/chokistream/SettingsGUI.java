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
	
	// These can throw exceptions in case the user inputs bad data (like "yummy" for the quality or something)
	public String getIp() throws InvalidOptionException {
		return ip;
	}
	public Mod getMod() throws InvalidOptionException {
		return mod;
	}
	public int getQuality() throws InvalidOptionException {
		return quality;
	}
	public NTRScreen getScreen() throws InvalidOptionException {
		return screen;
	}
	public int getPriority() throws InvalidOptionException {
		return priority;
	}
	public int getQos() throws InvalidOptionException {
		return qos;
	}
	public int getCapCPU() throws InvalidOptionException {
		return capCPU;
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
}
