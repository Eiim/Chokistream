package chokistream;

import java.io.IOException;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXVideo extends VideoOutputInterface {
	
	private ArrayList<Stage> stages = new ArrayList<>();
	private ImageView topImageView;
	private ImageView bottomImageView;
	double width = 400;
	double height = 240;
	
	public JavaFXVideo(StreamingInterface client, Layout layout) {
		super(client);
		
		System.out.println("Starting JFXV");
		
		switch(layout) {
			case SEPARATE:
				setupSeparate();
				break;
			case VERTICAL:
				setupVertical();
				break;
			default:
				displayError(new InvalidOptionException("Layout for JavaFXVideo", layout.toString()));
		}
	}
	
	public void renderFrame(Image im) {
		topImageView.setImage(im);
	}
	
	public void kill() {
		networkThread.interrupt();
		try {
			client.close();
		} catch (IOException e) {
			displayError(e);
		}
		for(Stage s : stages) {
			s.close();
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
		popup.show();
	}
	
	private void setupSeparate() {
		Stage topStage = new Stage();
		topStage.setWidth(400);
		topStage.setHeight(240);
		topImageView = new ImageView();
		Group gt = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt);
		topStage.setScene(st);
		topStage.setTitle("Chokistream - Top Screen");
		topStage.show();
		
		Stage bottomStage = new Stage();
		bottomStage.setWidth(320);
		bottomStage.setHeight(240);
		bottomImageView = new ImageView();
		Group gb = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		gb.getChildren().add(bottomImageView);
		Scene sb = new Scene(gb);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream - Bottom Screen");
		bottomStage.show();
		
		stages.add(topStage);
		stages.add(bottomStage);
	}
	
	private void setupVertical() {
		topImageView = new ImageView();
		bottomImageView = new ImageView();
		bottomImageView.relocate(40, 240);
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g);
		scene.setFill(Color.BLACK);
		
		Stage stage = new Stage();
		stage.setWidth(400);
		stage.setHeight(480);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
}
