package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.ColorMode;

public class ImageManipulator {
	
	private static Logger logger = Logger.INSTANCE;
	
	/**
	 * Rotates and color-adjusts an image
	 * @param in Original image
	 * @param cm Color adjustment mode
	 * @param swapRB Swap R/B channels (HzM/CHM)
	 * @return Adjusted image
	 */
	public static BufferedImage adjust(BufferedImage in, ColorMode cm, boolean swapRB) {
		BufferedImage out = new BufferedImage(in.getHeight(), in.getWidth(), in.getType());
		int ow = out.getWidth();
		int oh = out.getHeight();
		for(int i = 0; i < ow; i++) {
			for(int j = 0; j < oh; j++) {
				out.setRGB(i, oh-j-1, ColorHotfix.hotfixPixel(in.getRGB(j, i), cm, swapRB));
			}
		}
		return out;
	}
	
	/**
	 * Combines an image with another, doing interpolation, offset, color adjustment, and rotation, as necessary.
	 * @param base Image to be combined onto
	 * @param in Image to combine
	 * @param interlace Whether or not to interlace
	 * @param intrpParity Interlacing parity (0 or 1)
	 * @param offset x-offset for partial images
	 * @param cm Color adjustment mode
	 * @param swapRB Swap R/B channels (HzM/CHM)
	 * @return Resultant image
	 */
	public static BufferedImage adjust(BufferedImage base, BufferedImage in, boolean interlace, int interParity, int offset, ColorMode cm, boolean swapRB) throws ArrayIndexOutOfBoundsException {
		//logger.log(String.format("Composing %d x %d onto %d x %d with i=%s,%d, o=%d, cm=%s, and rb=%s", in.getWidth(), in.getHeight(), base.getWidth(), base.getHeight(), interlace, interParity, offset, cm.getLongName(), swapRB), LogLevel.VERBOSE);
		int iw = in.getWidth();
		int ih = in.getHeight();
		int oh = base.getHeight();
		for(int i = 0; i < iw; i++) {
			for(int j = 0; j < ih; j++) {
				int oy = interlace ? oh-(2*i)-interParity : oh-i-1; // Maybe should do branchless somehow? eh
				try {
					//logger.logOnce(in.getRGB(i, j)+"");
					base.setRGB(j + offset, oy, ColorHotfix.hotfixPixel(in.getRGB(i, j), cm, swapRB));
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log(i+" "+j+" "+(j+offset)+" "+oy);
					throw e;
				}
			}
		}
		return base;
	}

}
