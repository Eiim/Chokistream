package chokistream;

import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXVideo extends VideoOutputInterface {
	
	private Stage stage;
	private ImageView imageView;
	double width = 400;
	double height = 240;
	
	public JavaFXVideo(StreamingInterface client) {
		super(client);
		
		System.out.println("Starting JFXV");
		
		stage = new Stage();
		stage.setWidth(width);
		stage.setHeight(height);
		imageView = new ImageView();
		Group g = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		g.getChildren().add(imageView);
		Scene s = new Scene(g);
		stage.setScene(s);
		stage.setTitle("Chokistream");
		stage.show();
	}
	
	public void renderFrame(Image im) {
		if(im.getHeight() != height) {
			height = im.getHeight();
			stage.setHeight(height);
		}
		if(im.getWidth() != width) {
			width = im.getWidth();
			stage.setWidth(width);
		}
		imageView.setImage(im);
	}
	
	public void kill() {
		networkThread.interrupt();
		try {
			client.close();
		} catch (IOException e) {
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
}
