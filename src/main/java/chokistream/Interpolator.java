package chokistream;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
		
		WritableRaster r = in.getRaster();
		int oldW = in.getWidth();
		int oldH = in.getHeight();
		WritableRaster out = r.createCompatibleWritableRaster((int)(scale*oldW), (int)(scale*oldH));
		int[] storage = new int[r.getSampleModel().getNumBands()];
		
		for(int i = 0; i < oldW; i++) {
			for(int j = 0; j < oldH; j++) {
				int[] pixel = r.getPixel(i, j, storage);
				for(int i2 = (int)(scale*i); i2 < (int)(scale*(i+1)); i2++) {
					for(int j2 = (int)(scale*j); j2 < (int)(scale*(j+1)); j2++) {
						out.setPixel(i2, j2, pixel);
					}
				}
			}
		}
		
		return new BufferedImage(in.getColorModel(), out, in.isAlphaPremultiplied(), null); // I assume we never have any properties? or if we do they're not needed
	}
}
