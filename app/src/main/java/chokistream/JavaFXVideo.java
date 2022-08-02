package chokistream;

import java.awt.Toolkit;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXVideo extends VideoOutputInterface {
	
	private ArrayList<Stage> stages = new ArrayList<>();	
	private ImageView topImageView;
	private ImageView bottomImageView;
	private double uiScale;
		
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
		
		/*
		 * Transforms should be applied to the image in this order:
		 *  * Rotate
		 *  * Translate (layout)
		 *  * Translate (rotate/scale adjustment)
		 *  * Scale
		 * Because of the way transforms work, the easiest way to accomplish this
		 * is to add them to the transforms list in reverse order. Thus, we add the
		 * adjustment translate and the scale now, then add the layout translate
		 * during the layout setup, and then add the rotate at the end.
		 * 
		 * Note that the translates are interchangeable, and the rotate may be
		 * interchangeable with them as well, but I've spent so long getting this
		 * order than I'm not going to screw with it any more.
		 */
		
		// Scaling last is very important
		// If you don't do this you may spend hours of your life trying to
		// solve geometry formulas and multiplying matrices
		uiScale = 96/(double)Toolkit.getDefaultToolkit().getScreenResolution();
		topImageView.getTransforms().add(new Scale(uiScale, uiScale));
		bottomImageView.getTransforms().add(new Scale(uiScale, uiScale));
		
		// 240s come from the height of the screens
		topImageView.getTransforms().add(new Translate(0,240));
		bottomImageView.getTransforms().add(new Translate(0,240));
		
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
		
		// Set rotation, as the images come in rotated by 90 degrees.
		topImageView.getTransforms().add(new Rotate(-90));
		bottomImageView.getTransforms().add(new Rotate(-90));
		
		for(Stage stage : stages) {
			// Kill on close
			stage.setOnCloseRequest((e) -> {
	        	Platform.exit();
	        	System.exit(0);
	        });
			// Add screenshot trigger
			stage.getScene().setOnKeyPressed((e) -> {
				if(e.getCode() == KeyCode.S) {
					try {
						// Only take the screenshots if the image exists - mostly for HzMod, but perhaps
						// also if the images just haven't come yet because we're still initializing
						if(topImageView.getImage() != null) {
							WritableImage imgt = topImageView.snapshot(null, null);
							File ft = new File("chokistream_top.png");
							ImageIO.write(SwingFXUtils.fromFXImage(imgt, null), "png", ft);
						}
						if(bottomImageView.getImage() != null) {
							WritableImage imgb = bottomImageView.snapshot(null, null);
							File fb = new File("chokistream_bottom.png");
							ImageIO.write(SwingFXUtils.fromFXImage(imgb, null), "png", fb);
						}
						System.out.println("Took a screenshot!");
					} catch (IOException e1) {
						displayError(e1);
					}
				}
			});
			// Set black background
			stage.getScene().setFill(Color.BLACK);
			// Non-resizable
			stage.setResizable(false);
		}
		
		try {
	        Image logo64 = new Image(getClass().getResourceAsStream("/res/logo64.png"));
	        Image logo48 = new Image(getClass().getResourceAsStream("/res/logo48.png"));
	        Image logo32 = new Image(getClass().getResourceAsStream("/res/logo32.png"));
	        Image logo16 = new Image(getClass().getResourceAsStream("/res/logo16.png"));
	        
	        for(Stage stage : stages) {
	        	stage.getIcons().addAll(logo16, logo32, logo48, logo64);
	        }
        } catch(NullPointerException e) {
        	System.out.println("Couldn't find icons, most likely not running from jar");
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
		Group gt = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt, 400*uiScale, 240*uiScale);
		topStage.setScene(st);
		topStage.setTitle("Chokistream - Top Screen");
		topStage.show();
		
		Stage bottomStage = new Stage();
		Group gb = new Group(bottomImageView); // For some reason we can't use the imageView as root directly since it's not a parent
		Scene sb = new Scene(gb, 320*uiScale, 240*uiScale);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream - Bottom Screen");
		bottomStage.show();
		
		stages.add(topStage);
		stages.add(bottomStage);
	}
	
	private void setupVertical() {
		bottomImageView.getTransforms().add(new Translate(40, 240));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, 400*uiScale, 480*uiScale);
		
		Stage stage = new Stage();
		stage.setWidth(400);
		stage.setHeight(480);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupVerticalInv() {
		topImageView.getTransforms().add(new Translate(0, 240));
		bottomImageView.getTransforms().add(new Translate(40, 0));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, 400*uiScale, 480*uiScale);
		
		Stage stage = new Stage();
		stage.setWidth(400);
		stage.setHeight(480);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupHorizontal() {
		bottomImageView.getTransforms().add(new Translate(400, 0));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, 720*uiScale, 240*uiScale);
		
		Stage stage = new Stage();
		stage.setWidth(720);
		stage.setHeight(240);
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupHorizontalInv() {
		topImageView.getTransforms().add(new Translate(320, 0));
		Group g = new Group();
		g.getChildren().addAll(bottomImageView, topImageView);
		Scene scene = new Scene(g, 720*uiScale, 240*uiScale);
		
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
		Scene st = new Scene(gt, 400*uiScale, 240*uiScale);
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
		Scene sb = new Scene(gb, 400*uiScale, 240*uiScale);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream");
		bottomStage.show();
		
		stages.add(bottomStage);
	}
}
