package chokistream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXVideo extends VideoOutputInterface {
	
	private ArrayList<Stage> stages = new ArrayList<>();
	private ImageView topImageView;
	private ImageView bottomImageView;
		
	/**
	 * Instantiates a viewer using JavaFX.
	 * 
	 * @param client	The HzModClient or NTRClient to get frames from
	 * @param layout	The output layout configuration setting
	 */
	public JavaFXVideo(StreamingInterface client, Layout layout) {
		super(client);
		
		System.out.println("Starting JFXV");
		
		topImageView = new ImageView();
		bottomImageView = new ImageView();
		
		switch(layout) {
			case SEPARATE:
				setupSeparate();
				break;
			case VERTICAL:
				setupVertical();
				break;
			case VERTICAL_INV:
				setupVerticalInv();
				break;
			case HORIZONTAL:
				setupHorizontal();
				break;
			case HORIZONTAL_INV:
				setupHorizontalInv();
				break;
			case TOP_ONLY:
				setupTopOnly();
				break;
			case BOTTOM_ONLY:
				setupBottomOnly();
				break;
			default:
				displayError(new InvalidOptionException("Layout for JavaFXVideo", layout.toString()));
		}
		
		// Set rotation, as the images come in rotated 90 degrees
		// We need to rotate around (120,120) in local coordinates
		// 120 is derived from height / 2
		Rotate rotateT = new Rotate();
		rotateT.setPivotX(topImageView.getX()+120);
		rotateT.setPivotY(topImageView.getY()+120);
		rotateT.setAngle(-90);
		topImageView.getTransforms().add(rotateT);
		
		Rotate rotateB = new Rotate();
		rotateB.setPivotX(bottomImageView.getX()+120);
		rotateB.setPivotY(bottomImageView.getY()+120);
		rotateB.setAngle(-90);
		bottomImageView.getTransforms().add(rotateB);
		
		// Kill on close
		for(Stage stage : stages) {
			stage.setOnCloseRequest((e) -> {
	        	Platform.exit();
	        	System.exit(0);
	        });
			stage.getScene().setOnKeyPressed((e) -> {
				if(e.getCode() == KeyCode.S) {
					WritableImage imgt = topImageView.snapshot(null, null);
					File ft = new File("chokistream_top.png");
					WritableImage imgb = bottomImageView.snapshot(null, null);
					File fb = new File("chokistream_bottom.png");
					try {
						ImageIO.write(SwingFXUtils.fromFXImage(imgt, null), "png", ft);
						ImageIO.write(SwingFXUtils.fromFXImage(imgb, null), "png", fb);
						System.out.println("Took a screenshot!");
					} catch (IOException e1) {
						displayError(e1);
					}
				}
			});
		}
		
		networkThread.start();
	}
	
	/**
	 * Renders the given frame.
	 * 
	 * @param fr	The Frame to be rendered, including screen information.
	 */
	@Override
	public void renderFrame(Frame fr) {
		if(fr.screen == NTRScreen.BOTTOM) {
			bottomImageView.setImage(fr.image);
		} else {
			topImageView.setImage(fr.image);
		}
	}
	
	/**
	 * Attempts to kill the output windows, the thread communicating to the client,
	 * and the client itself.
	 */
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
	
	/**
	 * Generic pop-up error box
	 * 
	 * @param e	The exception to display the type and message of
	 */
	@Override
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
		Group gt = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt);
		topStage.setScene(st);
		topStage.setTitle("Chokistream - Top Screen");
		topStage.show();
		
		Stage bottomStage = new Stage();
		bottomStage.setWidth(320);
		bottomStage.setHeight(240);
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
	
	private void setupVerticalInv() {
		topImageView.relocate(0, 240);
		bottomImageView.relocate(40, 0);
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
	
	private void setupHorizontal() {
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g);
		scene.setFill(Color.BLACK);
		
		Stage stage = new Stage();
		stage.setWidth(720);
		stage.setHeight(240);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupHorizontalInv() {
		Group g = new Group();
		g.getChildren().addAll(bottomImageView, topImageView);
		Scene scene = new Scene(g);
		scene.setFill(Color.BLACK);
		
		Stage stage = new Stage();
		stage.setWidth(720);
		stage.setHeight(240);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupTopOnly() {
		Stage topStage = new Stage();
		topStage.setWidth(400);
		topStage.setHeight(240);
		Group gt = new Group();
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt);
		topStage.setScene(st);
		topStage.setTitle("Chokistream");
		topStage.show();
		
		stages.add(topStage);
	}
	
	private void setupBottomOnly() {
		Stage bottomStage = new Stage();
		bottomStage.setWidth(320);
		bottomStage.setHeight(240);
		Group gb = new Group();
		gb.getChildren().add(bottomImageView);
		Scene sb = new Scene(gb);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream");
		bottomStage.show();
		
		stages.add(bottomStage);
	}
}
