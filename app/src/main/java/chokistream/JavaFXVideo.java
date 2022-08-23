package chokistream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;
import chokistream.props.Layout;
import chokistream.props.LogLevel;
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
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JavaFXVideo extends VideoOutputInterface {
	
	private ArrayList<Stage> stages = new ArrayList<>();	
	private ImageView topImageView;
	private ImageView bottomImageView;
	private double uiScale;
	private static final Logger logger = Logger.INSTANCE;
	private double topScale;
	private double bottomScale;
	private int topFrames = 0;
	private int bottomFrames = 0;
	private int topFPS = 0;
	private int bottomFPS = 0;
	private Timer fpsTimer;
		
	/**
	 * Instantiates a viewer using JavaFX.
	 * 
	 * @param client	The HzModClient or NTRClient to get frames from
	 * @param layout	The output layout configuration setting
	 */
	public JavaFXVideo(App app, StreamingInterface client, Layout layout, int dpi, double topScale, double bottomScale, InterpolationMode intrp) {
		super(client);
		
		logger.log("Starting JFX Video", LogLevel.VERBOSE);
		
		topImageView = new ImageView();
		bottomImageView = new ImageView();
		topImageView.setCache(true);
		bottomImageView.setCache(true);
		
		this.topScale = topScale;
		this.bottomScale = bottomScale;
		
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
		uiScale = 96.0/dpi;
		topImageView.getTransforms().add(new Scale(uiScale, uiScale));
		bottomImageView.getTransforms().add(new Scale(uiScale, uiScale));
		
		// Temporary until we can get better algorithms in place
		//topImageView.getTransforms().add(new Scale(topScale, topScale));
		//bottomImageView.getTransforms().add(new Scale(bottomScale, bottomScale));
		
		// 240s come from the height of the screens
		topImageView.getTransforms().add(new Translate(0,240*topScale));
		bottomImageView.getTransforms().add(new Translate(0,240*bottomScale));
		
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
				logger.close();
				fpsTimer.cancel();
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
						logger.log("Took a screenshot!");
					} catch (IOException e1) {
						displayError(e1);
					}
				} else if(e.getCode() == KeyCode.UP) {
					if(client instanceof HZModClient) {
						HZModClient c2 = HZModClient.class.cast(client);
						if(c2.quality < 100) {
							c2.quality++;
							try {
								c2.sendQuality(c2.quality);
							} catch (IOException e1) {
								displayError(e1);
							}
						}
					}
				} else if(e.getCode() == KeyCode.DOWN) {
					if(client instanceof HZModClient) {
						HZModClient c2 = HZModClient.class.cast(client);
						if(c2.quality > 0) {
							c2.quality--;
							try {
								c2.sendQuality(c2.quality);
							} catch (IOException e1) {
								displayError(e1);
							}
						}
					}
				} else if(e.getCode() == KeyCode.BACK_SPACE) {
					kill();
					app.reopen();
				}
			});
			// Set black background
			stage.getScene().setFill(Color.BLACK);
			// Non-resizable
			stage.setResizable(false);
			// Add icons
			IconLoader.applyFavicon(stage);
		}
		
		fpsTimer = new Timer();
		fpsTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				topFPS = topFrames;
				bottomFPS = bottomFrames;
				topFrames = 0;
				bottomFrames = 0;
				
				// We can't update it from this thread, but Platform.runLater allows us to request an event on the main thread
				Platform.runLater(new Runnable() {
					public void run() {
						if(stages.size() == 2) {
							stages.get(0).setTitle("Chokistream - Top Screen ("+topFPS+" FPS)");
							stages.get(1).setTitle("Chokistream - Bottom Screen ("+bottomFPS+" FPS)");
						} else {
							stages.get(0).setTitle("Chokistream ("+(int)Math.max(topFPS, bottomFPS)+" FPS)");
						}
					}
				});
			}
		}, 1000, 1000);
		
		networkThread.start();
	}
	
	/**
	 * Renders the given frame.
	 * 
	 * @param fr	The Frame to be rendered, including screen information.
	 */
	@Override
	public void renderFrame(Frame fr) {
		if(fr.screen == DSScreen.BOTTOM) {
			bottomImageView.setImage(SwingFXUtils.toFXImage(fr.image, null));
			bottomFrames++;
		} else {
			topImageView.setImage(SwingFXUtils.toFXImage(fr.image, null));
			topFrames++;
		}
	}
	
	/**
	 * Attempts to kill the output windows, the thread communicating to the client,
	 * and the client itself.
	 */
	public void kill() {
		fpsTimer.cancel();
		networkThread.stopRunning();
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
		e.printStackTrace();
		// Ensure that we're on the right thread
		Platform.runLater(() -> {
			Stage popup = new Stage();
			popup.initModality(Modality.APPLICATION_MODAL);
			Label message = new Label(e.getClass().getSimpleName()+": "+e.getMessage());
			message.setPadding(new Insets(7));
			Scene scene = new Scene(message);
			popup.setScene(scene);
			popup.setTitle("Error");
			popup.show();
		});
	}
	
	private void setupSeparate() {
		Stage topStage = new Stage();
		Group gt = new Group(); // For some reason we can't use the imageView as root directly since it's not a parent
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt, 400*uiScale*topScale, 240*uiScale*topScale);
		topStage.setScene(st);
		topStage.setTitle("Chokistream - Top Screen");
		topStage.show();
		
		Stage bottomStage = new Stage();
		Group gb = new Group(bottomImageView); // For some reason we can't use the imageView as root directly since it's not a parent
		Scene sb = new Scene(gb, 320*uiScale*bottomScale, 240*uiScale*bottomScale);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream - Bottom Screen");
		bottomStage.show();
		
		stages.add(topStage);
		stages.add(bottomStage);
	}
	
	private void setupVertical() {
		int maxWidth = (int) Math.round(Math.max(400*topScale, 320*bottomScale));
		bottomImageView.getTransforms().add(new Translate((maxWidth-bottomScale*320)/2, 240*topScale));
		topImageView.getTransforms().add(new Translate((maxWidth-topScale*400)/2, 0));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, maxWidth*uiScale, 240*uiScale*topScale+240*uiScale*bottomScale);
		
		Stage stage = new Stage();
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupVerticalInv() {
		int maxWidth = (int) Math.round(Math.max(400*topScale, 320*bottomScale));
		topImageView.getTransforms().add(new Translate((maxWidth-topScale*400)/2, 240*bottomScale));
		bottomImageView.getTransforms().add(new Translate((maxWidth-bottomScale*320)/2, 0));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, maxWidth*uiScale, 240*uiScale*topScale+240*uiScale*bottomScale);
		
		Stage stage = new Stage();
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	// TODO: known bug when bottom screen is larger than top
	private void setupHorizontal() {
		int maxHeight = (int) Math.round(Math.max(topScale, bottomScale)*240);
		bottomImageView.getTransforms().add(new Translate(400*topScale, (maxHeight-bottomScale*240)/2));
		topImageView.getTransforms().add(new Translate(0, (maxHeight-topScale*240)/2));
		Group g = new Group();
		g.getChildren().addAll(topImageView, bottomImageView);
		Scene scene = new Scene(g, 400*uiScale*topScale+320*uiScale*bottomScale, maxHeight*uiScale);
		
		Stage stage = new Stage();
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	// TODO: known bug when top screen is larger than bottom
	private void setupHorizontalInv() {
		int maxHeight = (int) Math.round(Math.max(topScale, bottomScale)*240);
		topImageView.getTransforms().add(new Translate(320*bottomScale, (maxHeight-topScale*240)/2));
		bottomImageView.getTransforms().add(new Translate(0, (maxHeight-bottomScale*240)/2));
		Group g = new Group();
		g.getChildren().addAll(bottomImageView, topImageView);
		Scene scene = new Scene(g, 400*uiScale*topScale+320*uiScale*bottomScale, maxHeight*uiScale);
		
		Stage stage = new Stage();
		stage.setTitle("Chokistream");
		stage.setScene(scene);
		stage.show();
		
		stages.add(stage);
	}
	
	private void setupTopOnly() {
		Stage topStage = new Stage();
		Group gt = new Group();
		gt.getChildren().add(topImageView);
		Scene st = new Scene(gt, 400*uiScale*topScale, 240*uiScale*topScale);
		topStage.setScene(st);
		topStage.setTitle("Chokistream");
		topStage.show();
		
		stages.add(topStage);
	}
	
	private void setupBottomOnly() {
		Stage bottomStage = new Stage();
		Group gb = new Group();
		gb.getChildren().add(bottomImageView);
		Scene sb = new Scene(gb, 400*uiScale*bottomScale, 240*uiScale*bottomScale);
		bottomStage.setScene(sb);
		bottomStage.setTitle("Chokistream");
		bottomStage.show();
		
		stages.add(bottomStage);
	}
}
