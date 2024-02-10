package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.InterpolationMode;

public class Interpolator {
	
	public static BufferedImage scale(BufferedImage in, InterpolationMode mode, double scale) {
		switch(mode) {
			case NONE:
				return nearestNeighbor(in, scale);
			default:
				return nearestNeighbor(in, scale);
		}
	}
	
	private static BufferedImage nearestNeighbor(BufferedImage in, double scale) {
		if(in == null) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		int oldW = in.getWidth();
		int oldH = in.getHeight();
		BufferedImage out = new BufferedImage((int)(scale*oldW), (int)(scale*oldH), in.getType());
		for(int i = 0; i < oldW; i++) {
			for(int j = 0; j < oldH; j++) {
				int rgb = in.getRGB(i, j);
				for(int i2 = (int)(scale*i); i2 < (int)(scale*(i+1)); i2++) {
					for(int j2 = (int)(scale*j); j2 < (int)(scale*(j+1)); j2++) {
						out.setRGB(i2, j2, rgb);
					}
				}
			}
		}
		return out;
	}
}
