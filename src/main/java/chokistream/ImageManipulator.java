package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.ColorMode;

public class ImageManipulator {
	
	private static Logger logger = Logger.INSTANCE;
	
	/**
	 * For NTR: rotates and color-adjusts an image
	 * @param in Original image
	 * @param cm Color adjustment mode
	 * @return Adjusted image
	 */
	public static BufferedImage adjust(BufferedImage in, ColorMode cm) {
		BufferedImage out = new BufferedImage(in.getHeight(), in.getWidth(), in.getType());
		if(cm == ColorMode.REGULAR) {
			return adjustStandard(out, in, 0, 0);
		} else {
			return adjustCM(out, in, 0, cm);
		}
	}
	
	/**
	 * Combines an image with another, doing interlacing, offset, color adjustment, and rotation, as necessary.
	 * Propagates down to optimized code paths.
	 * @param base Image to be combined onto
	 * @param in Image to combine
	 * @param interlace Whether or not to interlace
	 * @param intrpParity Interlacing parity (0 or 1)
	 * @param offset x-offset for partial images
	 * @param cm Color adjustment mode
	 * @param swapRB Swap R/B channels (HzM/CHM)
	 * @return Resultant image
	 */
	public static BufferedImage adjust(BufferedImage base, BufferedImage in, boolean interlace, int interParity, int offsY, int offsX, ColorMode cm, boolean swapRB) {
		if(interlace) {
			if(swapRB) {
				if(cm == ColorMode.REGULAR) {
					return adjustIntRB(base, in, interParity, offsY);
				} else {
					return adjustIntCMRB(base, in, interParity, offsY, cm);
				}
			} else {
				if(cm == ColorMode.REGULAR) {
					return adjustInt(base, in, interParity, offsY);
				} else {
					return adjustIntCM(base, in, interParity, offsY, cm);
				}
			}
		} else {
			if(swapRB) {
				if(cm == ColorMode.REGULAR) {
					return adjustRB(base, in, offsY, offsX);
				} else {
					return adjustCMRB(base, in, offsY, cm);
				}
			} else {
				if(cm == ColorMode.REGULAR) {
					return adjustStandard(base, in, offsY, offsX);
				} else {
					return adjustCM(base, in, offsY, cm);
				}
			}
		}
	}
	
	// Only offset + rotation
	// TODO: Make faster with Raster
	private static BufferedImage adjustStandard(BufferedImage base, BufferedImage in, int offsY, int offsX) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < ih; i++) {
			for(int j = 0; j < iw; j++) {
				try {
					base.setRGB(i + offsY, iw-j-1+offsX, in.getRGB(j, i));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log("i="+i+", j="+j+", offsY="+(offsY)+", offsX="+(offsX)+", iw-j-1="+(iw-j-1));
					//logger.log("ImageManipulator.java: "+e.getClass()+": "+e.getMessage());
					//logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
					break; // skip the rest of this row (or "row")
					//throw e;
				}
				
			}
		}
		return base;
	}
	
	// interlace + offset + rotation
	// TODO: Make faster with Raster
	private static BufferedImage adjustInt(BufferedImage base, BufferedImage in, int interParity, int offset) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				try {
					base.setRGB(j + offset, 2*(iw-i)-interParity-1, in.getRGB(i, j));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log(i+" "+j+" "+(j+offset)+" "+(iw-(2*i)-interParity-1));
					throw e;
				}
			}
		}
		return base;
	}
	
	// interlace + offset + rotation + colormode
	private static BufferedImage adjustIntCM(BufferedImage base, BufferedImage in, int interParity, int offset, ColorMode cm) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				try {
					base.setRGB(j + offset, 2*(iw-i)-interParity-1, ColorHotfix.hotfixPixel(in.getRGB(i, j), cm));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log(i+" "+j+" "+(j+offset)+" "+(iw-(2*i)-interParity-1));
					throw e;
				}
			}
		}
		return base;
	}
	
	// interlace + offset + rotation + swap rb
	private static BufferedImage adjustIntRB(BufferedImage base, BufferedImage in, int interParity, int offset) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				try {
					base.setRGB(j + offset, 2*(iw-i)-interParity-1, ColorHotfix.hzModSwapRedBlue(in.getRGB(i, j)));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log(i+" "+j+" "+(j+offset)+" "+(iw-(2*i)-interParity-1));
					throw e;
				}
			}
		}
		return base;
	}
	
	// interlace + offset + rotation + swap rb + colormode
	private static BufferedImage adjustIntCMRB(BufferedImage base, BufferedImage in, int interParity, int offset, ColorMode cm) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				try {
					base.setRGB(j + offset, 2*(iw-i)-interParity-1, ColorHotfix.hotfixPixel(ColorHotfix.hzModSwapRedBlue(in.getRGB(i, j)), cm));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log(i+" "+j+" "+(j+offset)+" "+(iw-(2*i)-interParity-1));
					throw e;
				}
			}
		}
		return base;
	}
	
	// offset + rotation + colormode
	private static BufferedImage adjustCM(BufferedImage base, BufferedImage in, int offset, ColorMode cm) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				base.setRGB(j + offset, iw-i-1, ColorHotfix.hotfixPixel(in.getRGB(i, j), cm));
			}
		}
		return base;
	}
	
	// offset + rotation + swap rb
	private static BufferedImage adjustRB(BufferedImage base, BufferedImage in, int offsY, int offsX) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				try {
					base.setRGB(j + offsY, iw-i-1+offsX, ColorHotfix.hzModSwapRedBlue(in.getRGB(i, j)));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log("i="+i+", j="+j+", offsY="+(offsY)+", offsX="+(offsX)+", iw-i-1="+(iw-i-1));
					//logger.log("ImageManipulator.java: "+e.getClass()+": "+e.getMessage());
					//logger.log(Arrays.toString(e.getStackTrace()), LogLevel.EXTREME);
					//break; // skip the rest of this row (or "row")
					throw e;
				}
			}
		}
		return base;
	}
	
	// offset + rotation + swap rb + colormode
	private static BufferedImage adjustCMRB(BufferedImage base, BufferedImage in, int offset, ColorMode cm) {
		int iw = in.getWidth();
		int ih = in.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				base.setRGB(j + offset, iw-i-1, ColorHotfix.hotfixPixel(ColorHotfix.hzModSwapRedBlue(in.getRGB(i, j)), cm));
			}
		}
		return base;
	}
	
	public static BufferedImage combineVert(BufferedImage top, BufferedImage bottom) {
		BufferedImage out = new BufferedImage(Math.max(top.getWidth(), bottom.getWidth()), top.getHeight()+bottom.getHeight(), top.getType());
		
		int tx = (out.getWidth() - top.getWidth())/2;
		int by = top.getHeight();
		int bx = (out.getWidth() - bottom.getWidth())/2;
		
		int tw = top.getWidth();
		int th = top.getHeight();
		for(int i = 0; i < tw;  i++) {
			for(int j = 0; j < th; j++) {
				out.setRGB(i+tx, j, top.getRGB(i, j));
			}
		}
		
		int bw = bottom.getWidth();
		int bh = bottom.getHeight();
		for(int i = 0; i < bw;  i++) {
			for(int j = 0; j < bh; j++) {
				out.setRGB(i+bx, j+by, bottom.getRGB(i, j));
			}
		}
		
		return out;
	}
	
	public static BufferedImage combineHoriz(BufferedImage left, BufferedImage right) {
		BufferedImage out = new BufferedImage(left.getWidth()+right.getWidth(), Math.max(left.getHeight(), right.getHeight()), left.getType());
		
		int ly = (out.getHeight() - left.getHeight())/2;
		int rx = left.getWidth();
		int ry = (out.getHeight() - right.getHeight())/2;
		
		int lw = left.getWidth();
		int lh = left.getHeight();
		for(int i = 0; i < lw;  i++) {
			for(int j = 0; j < lh; j++) {
				out.setRGB(i, j+ly, left.getRGB(i, j));
			}
		}
		
		int rw = right.getWidth();
		int rh = right.getHeight();
		for(int i = 0; i < rw;  i++) {
			for(int j = 0; j < rh; j++) {
				out.setRGB(i+rx, j+ry, right.getRGB(i, j));
			}
		}
		
		return out;
	}

}
