package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.DSScreen;

/**
 * Represents a frame received from a client.
 */
public class Frame {
	/**
	 * Which NTRScreen this Frame holds.
	 * Defaults to NTRScreen.TOP for HZModClient.
	 */
	public DSScreen screen = DSScreen.TOP;
	
	/**
	 * The Image received from a client.
	 */
	public BufferedImage image;
	
	/**
	 * HZMod version:
	 * Create a new Frame object.
	 * @param _image The Image to load into the Frame.
	 */
	public Frame(BufferedImage _image) {
		image = _image;
	}
	
	/**
	 * NTR version:
	 * Create a new Frame object.
	 * @param _screen Which NTRScreen this Frame holds.
	 * @param _image The Image to load into the Frame.
	 */
	public Frame(DSScreen _screen, BufferedImage _image) {
		screen = _screen;
		image = _image;
	}

}
