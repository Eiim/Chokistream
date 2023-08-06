package chokistream;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import chokistream.props.DSScreen;
import chokistream.props.InterpolationMode;

public class ImageComponent extends JComponent {

	// Unlikely to matter but good practice, randomly generated
	private static final long serialVersionUID = 765806607527369338L;
	
	private BufferedImage img;
	private DSScreen screen;
	private int width;
	private int height;
	private InterpolationMode intrp;

	public ImageComponent(DSScreen screen, double scale, InterpolationMode intrp) {
		this.screen = screen;
		this.intrp = intrp;
		width = (int)((screen == DSScreen.TOP ? 400 : 320) * scale);
		height = (int) (240 * scale);
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		setOpaque(true);
	}
	
	@Override
	public void paintComponent(Graphics g)
    {
        g.drawImage(processImage(img), 0, 0, null);
    }
	
	public void updateImage(BufferedImage image) {
		img = image;
		repaint();
	}
	
	private BufferedImage processImage(BufferedImage in) {
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		double hscale = (double)width / in.getHeight();
		double vscale = (double)height / in.getWidth();
		Logger.INSTANCE.log(width+" "+height+" "+hscale+" "+vscale+" "+in.getHeight()+" "+in.getWidth());
		switch(intrp) {
		case NONE:
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					try {
						out.setRGB(i, height-j-1, in.getRGB((int)(j / hscale), (int)(i / vscale)));
					} catch(ArrayIndexOutOfBoundsException e) {
						if(j == 0 || j == height-1) {}
							//System.out.println(i+" "+j+" "+(int)(j/hscale)+" "+(int)(i/vscale)+" "+in.getWidth()+" "+in.getHeight());
					}
				}
			}
		default:
			Logger.INSTANCE.logOnce("Unsupported interpretation mode "+intrp.getLongName());
		}
		return out;
	}
	
	public Image getImage() {
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
