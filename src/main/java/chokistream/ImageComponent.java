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
	private final DSScreen screen;
	private final InterpolationMode intrp;
	private final int width;
	private final int height;
	private final double scale;

	public ImageComponent(DSScreen screen, double scale, InterpolationMode intrp) {
		this.screen = screen;
		this.intrp = intrp;
		this.scale = scale;
		width = (int)(854 * scale);
		height = (int)(480 * scale);
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
