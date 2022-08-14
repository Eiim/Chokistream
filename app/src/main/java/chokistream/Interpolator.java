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
		int newW = (int)(scale*in.getWidth());
		int newH = (int)(scale*in.getHeight());
		BufferedImage out = new BufferedImage(newW, newH, in.getType());
		for(int i = 0; i < newW; i++) {
			for(int j = 0; j < newH; j++) {
				out.setRGB(i, j, in.getRGB((int)(i/scale), (int)(j/scale)));
			}
		}
		return out;
	}
}
