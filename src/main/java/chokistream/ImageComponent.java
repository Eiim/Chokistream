package chokistream;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import chokistream.props.DSScreen;

public class ImageComponent extends JComponent {

	// Unlikely to matter but good practice, randomly generated
	private static final long serialVersionUID = 765806607527369338L;
	
	private BufferedImage img;
	private DSScreen screen;
	private int width;
	private int height;

	public ImageComponent(DSScreen screen, double scale) {
		this.screen = screen;
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
		// Ignore weird images
		if(image.getWidth() == 240) {
			img = processImage(image);
			repaint();
		} else {
			Logger.INSTANCE.log("Unexpected frame dimensions: "+image.getHeight()+"x"+image.getWidth());
		}
	}
	
	private BufferedImage processImage(BufferedImage in) {
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		double hscale = (double)width / in.getHeight();
		double vscale = (double)height / in.getWidth();
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				try {
					out.setRGB(i, height-j-1, in.getRGB((int)(j / hscale), (int)(i / vscale)));
				} catch(ArrayIndexOutOfBoundsException e) {
					//System.out.println(i+" "+j+" "+(int)(j/hscale)+" "+(int)(i/vscale)+" "+in.getWidth()+" "+in.getHeight());
				}
			}
		}
		return out;
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
