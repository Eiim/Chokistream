package chokistream;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;

public class ImageComponent extends JComponent {

	// Unlikely to matter but good practice, randomly generated
	private static final long serialVersionUID = 765806607527369338L;
	
	private BufferedImage img;
	private DSScreen screen;
	private InterpolationMode intrp;
	private int width;
	private int height;
	private double scale;

	public ImageComponent(DSScreen screen, double scale, InterpolationMode intrp) {
		this.screen = screen;
		this.intrp = intrp;
		this.scale = scale;
		width = (int)((screen == DSScreen.TOP ? 400 : 320) * scale);
		height = (int) (240 * scale);
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		setOpaque(true);
	}
	
	@Override
	public void paintComponent(Graphics g)
    {
        g.drawImage(img, 0, 0, null);
    }
	
	public void updateImage(BufferedImage image) {
		img = Interpolator.scale(image, intrp, scale);
		repaint();
	}
	
	public BufferedImage getImage() {
		return img;
	}
	
	public DSScreen getScreen() {
		return screen;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
