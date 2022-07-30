package chokistream;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsGUI extends Scene {
	
	public SettingsGUI(Parent p, double width, double height) {
		super(p, width, height);
	}
	
	private String ip = "0.0.0.0";
	private Mod mod = Mod.NTR;
	private int quality = 70;
	private NTRScreen screen = NTRScreen.TOP;
	private int priority = 8;
	private int qos = 26;
	private int capCPU  = 0;
	
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
	
	public void displayError(Exception e) {
		Stage popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		Label message = new Label(e.getMessage());
		Scene scene = new Scene(message);
		popup.setScene(scene);
		popup.show();
	}
}
