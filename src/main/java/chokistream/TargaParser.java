package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.DSScreen;
import chokistream.props.LogLevel;

public class TargaParser {
	
	private static final Logger logger = Logger.INSTANCE;
	
	public static BufferedImage parseBytes(byte[] data, DSScreen screen, TGAPixelFormat format) {
		//int width = 240;
		//int height = screen == DSScreen.BOTTOM ? 320 : 400;
		
		int height = (data[15] & 0xff) * 256 + (data[14] & 0xff);
		if(height < 1 || height > 400 ) {
			logger.log("Warning: invalid \"height\" in Targa metadata. height="+height, LogLevel.VERBOSE);
			height = screen == DSScreen.BOTTOM ? 320 : 400;
		}
		int width = (data[13] & 0xff) * 256 + (data[12] & 0xff);
		if(width < 1 || width > 240) {
			logger.log("Warning: invalid \"width\" in Targa metadata. width="+width, LogLevel.VERBOSE);
			width = 240;
		}
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		if(data[1] != 0x00) {
			logger.log("Warning: Unexpected color-mapped image. Function not implemented. colormaptype="+(data[1] & 0xff));
		}
		
		if(data[2] != 0x0A) {
			logger.log("Warning: reported image type is not BGR_RLE. Function not implemented. imagetype="+(data[2] & 0xff));
		}
		
		//int offset = (data[10] & 0xff) * 256 + (data[11] & 0xff);
		
		int rf = -1;
		switch(data[16] & 0xff) {
			case 16:
				rf = 3;
				break;
			case 17:
				rf = 2;
				break;
			case 18:
				rf = 4;
				break;
			case 24:
				rf = 1;
				break;
			case 32:
				rf = 5;
				break;
			default:
				rf = -1;
				break;
		}
		
		if(rf == -1) {
			logger.log("Warning: Pixel format specified in Targa metadata is invalid. Falling back... reported_bpp="+(data[16] & 0xff)+"; format="+TGAPixelFormat.toString(format));
		} else {
			if(TGAPixelFormat.fromInt(rf) != format) {
				logger.log("Warning: Pixel format specified in Targa metadata differs from previously specified format. tga_reported_format="+TGAPixelFormat.toString(TGAPixelFormat.fromInt(rf))+"; format="+TGAPixelFormat.toString(format));
			}
			format = TGAPixelFormat.fromInt(rf);
		}
		logger.log("format="+TGAPixelFormat.toString(format), LogLevel.EXTREME);
		
		int attrbits = data[17] & 0b00001111;
		logger.log("attrbits="+(attrbits), LogLevel.EXTREME);
		if(attrbits != 0) {
			//Logger.INSTANCE.log("Warning: \"Number of attribute bits per pixel\" is not zero. Function not implemented. attrbits="+(attrbits));
		}

		int idfieldlength = data[0] & 0xff;
		int startingoffset = 18 + idfieldlength; // This should be correct... Formerly hardcoded to 22.
		logger.log("\"Image ID\" field length="+(idfieldlength), LogLevel.EXTREME);
		
		boolean formatswitched = false;
		if(format == TGAPixelFormat.RGB8) {
			format = TGAPixelFormat.RGBA8;
			formatswitched = true;
		}
		
		int pxnum = 0;
		for(int i = startingoffset; i < data.length - (startingoffset + 4) && pxnum < width*height;) {
			byte header = data[i];
			boolean rle = (header & 0x80) == 0x80; // Top bit is one
			int packlen = (header & 0x7F) + 1; // Bottom 15 bits plus one
			
			// debug
			if(format == TGAPixelFormat.RGB8) {
				//rle = false;
			}
			if(format == TGAPixelFormat.RGBA8) {
				rle = false;
			}
			
			if(rle) {
				i += 1;
				int[] colorDat = new int[format.bytes];
				for(int k = 0; k < format.bytes; k++) {
					try {
						colorDat[k] = data[i+k] & 0xff;
					} catch(ArrayIndexOutOfBoundsException e) {
						colorDat[k] = 0;
						logger.log(e.getMessage());
					}
				}
				int[] rgb = getRGB(colorDat, format);
				int r = rgb[0];
				int g = rgb[1];
				int b = rgb[2];
				
				// Repeat for as many times as are in packlen
				for(int pn = 0; pn < packlen; pn++) {
					// Maybe should double-check that we haven't overrun here
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.log(e.getMessage());
						// TODO: error handling?
					}
					pxnum++;
				}
				i += format.bytes;
			} else {
				i += 1;
				for(int j = 0; j < packlen; j++) {
					int[] colorDat = new int[format.bytes];
					for(int k = 0; k < format.bytes; k++) {
						try {
							colorDat[k] = data[i+k] & 0xff;
						} catch(ArrayIndexOutOfBoundsException e) {
							colorDat[k] = 0;
							logger.log(e.getMessage());
						}
					}
					int[] rgb = getRGB(colorDat, format);
					int r = rgb[0];
					int g = rgb[1];
					int b = rgb[2];
					
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.log(e.getMessage());
						// TODO: error handling?
					}
					pxnum++;
					i += format.bytes;
				}
			}
		}
		if(formatswitched) {
			format = TGAPixelFormat.RGB8;
		}
		return image;
	}
	
	private static int[] getRGB(int[] bytes, TGAPixelFormat format) {
		int r=0,g=0,b=0;
		switch(format) {
			case RGB565:
				// RRRRRGGG GGGBBBBB
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 3) | ((bytes[0] & 0b11100000) >>> 5);
				b = bytes[0] & 0b00011111;
				// Scale from 5/6 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 2) | (g >>> 4);
				b = (b << 3) | (b >>> 2);
				break;
			case RGB5A1:
				// RRRRRGGG GGBBBBBA
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 2) | ((bytes[0] & 0b11000000) >>> 6);
				b = (bytes[0] & 0b00111110) >>> 1;
				// Scale from 5 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 3) | (g >>> 2);
				b = (b << 3) | (b >>> 2);
				break;
			case RGBA4:
				// AAAABBBB GGGGRRRR
				r = bytes[0] & 0b00001111;
				g = (bytes[0] & 0b11110000) >>> 4;
				b = bytes[1] & 0b00001111;
				// Scale from 4 bits to 8 bits
				r = (r << 4) | r;
				g = (g << 4) | g;
				b = (b << 4) | b;
				break;
			case RGB8:
				// RRRRRRRRR GGGGGGGG BBBBBBBB
				r = bytes[2];
				g = bytes[1];
				b = bytes[0];
				break;
			case RGBA8:
				// AAAAAAAA BBBBBBBB GGGGGGGG RRRRRRRR
				r = bytes[0];
				g = bytes[1];
				b = bytes[2];
				break;
		}
		return new int[] {r,g,b};
	}
}
