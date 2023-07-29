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
			logger.log("Warning: invalid \"height\" in Targa metadata. height="+height);
			height = screen == DSScreen.BOTTOM ? 320 : 400;
		}
		int width = (data[13] & 0xff) * 256 + (data[12] & 0xff);
		if(width < 1 || width > 256) {
			logger.log("Warning: invalid \"width\" in Targa metadata. width="+width, LogLevel.VERBOSE);
			width = 240;
		}
		
		logger.log("height="+height, LogLevel.EXTREME);
		logger.log("width="+width, LogLevel.EXTREME);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		if(data[1] != 0x00) {
			logger.log("Warning: Unexpected color-mapped image. Function not implemented. colormaptype="+(data[1] & 0xff));
		}
		
		if(data[2] != 0x0A) {
			logger.log("Warning: reported image type is not BGR_RLE. Function not implemented. imagetype="+(data[2] & 0xff));
		}
		
		//int origin_y = (data[10] & 0xff) * 256 + (data[11] & 0xff);
		
		int tgaBpp = data[16] & 0xff;
		TGAPixelFormat tgaReportedFormat = TGAPixelFormat.RGB8; // placeholder
		boolean tgaBppErr = false;
		switch(tgaBpp) {
			case 16:
				tgaReportedFormat = TGAPixelFormat.RGB5A1;
				break;
			case 17:
				tgaReportedFormat = TGAPixelFormat.RGB565;
				break;
			case 18:
				tgaReportedFormat = TGAPixelFormat.RGBA4;
				break;
			case 24:
				tgaReportedFormat = TGAPixelFormat.RGB8;
				break;
			case 32:
				tgaReportedFormat = TGAPixelFormat.RGBA8;
				break;
			case 8:
				logger.log("Warning: Bit-depth \"BPP=8\" specified in Targa metadata. Function not implemented. (This error is common and can be safely ignored)", LogLevel.EXTREME);
				format = TGAPixelFormat.RGB5A1; // good enough error-handling
				tgaBppErr = true;
				break;
			default:
				logger.log("Warning: Invalid bit-depth \"BPP="+tgaBpp+"\" specified in Targa metadata. Falling back to "+format+" ...");
				tgaBppErr = true;
				break;
		}
		
		boolean isMalformed24bpp = false;
		
		// this is broken somehow, but the issue seems to be self-contained
		if(!tgaBppErr) {
			if(tgaReportedFormat != format) {
				if(tgaReportedFormat == TGAPixelFormat.RGBA8 && format == TGAPixelFormat.RGB8) {
					logger.log("Warning: Format is RGBA8 (32bpp) but MODESET packet mis-reports RGB8 (24bpp). This is usually safe to ignore.", LogLevel.EXTREME);
					isMalformed24bpp = true;
				}
				logger.log("Warning: Color format specified in Targa metadata ("+tgaReportedFormat+") differs from format specified by MODESET packet ("+format+")");
				format = tgaReportedFormat;
			}
		}
		
		
		
		int attrbits = (data[17] & 0xff) & 0b00001111;
		logger.log("attrbits="+(attrbits), LogLevel.EXTREME);
		if(attrbits != 0) {
			//Logger.INSTANCE.log("Warning: \"Number of attribute bits per pixel\" is not zero. Function not implemented. attrbits="+(attrbits));
		}
		
		logger.log("format="+format, LogLevel.EXTREME);

		int idfieldlength = data[0] & 0xff;
		int startingoffset = 18 + idfieldlength; // This should be correct... Formerly hardcoded to 22.
		logger.log("idfieldlength="+(idfieldlength), LogLevel.EXTREME);
		int endOfImgDataOffset = data.length - 26; // footer (26 bytes) + other areas (which probably aren't present)
		
		
		if(isMalformed24bpp) {
			format = TGAPixelFormat.RGBA8;
		}
		
		byte[] decBuf = new byte[400 * 256 * 4]; // middle-man decode buffer
		decBuf = tgaDecode(data, decBuf, format, width, height, startingoffset, endOfImgDataOffset);
		
		if(isMalformed24bpp) {
			format = TGAPixelFormat.RGB8;
		}
		
		image = tgaTranslate(decBuf, image, format, width, height);
		

		/*
		int pxnum = 0;
		for(int i = startingoffset; i < endOfImgDataOffset && pxnum < width*height;) {
			boolean errorout = false;
			byte header = data[i];
			boolean rle = (header & 0x80) == 0x80; // Top bit is one
			int packlen = (header & 0x7F) + 1; // Bottom 15 bits plus one
			
			if(rle) {
				i += 1;
				int[] colorDat = new int[format.bytes];
				for(int k = 0; k < format.bytes; k++) {
					try {
						colorDat[k] = data[i+k] & 0xff;
					} catch(ArrayIndexOutOfBoundsException e) {
						colorDat[k] = 0;
						logger.log("Error: Reached end of image data while decoding colors of RLE packet. ("+e.getMessage()+")");
						// fill the rest of the colors with 0. draw pixels (unless all colors are zero). then break out of the larger for-loop.
						errorout = true;
						break;
					}
				}
				int[] rgb = getRGB(colorDat, format);
				int r = rgb[0];
				int g = rgb[1];
				int b = rgb[2];
				
				if(errorout == true && r == 0 && g == 0 && b == 0) {
					break;
				}
				
				// Repeat for as many times as are in packlen
				for(int pn = 0; pn < packlen; pn++) {
					// Maybe should double-check that we haven't overrun here
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.log("Error: Reached end of image buffer while writing pixels of RLE packet. ("+e.getMessage()+")");
						// break out of the larger for-loop.
						errorout = true;
						break;
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
							logger.log("Error: Reached end of image data while decoding pixels of RAW packet. ("+e.getMessage()+")");
							// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger for-loop.
							errorout = true;
							while(k < format.bytes) {
								colorDat[k] = 0;
								k++;
							}
							break;
						}
					}
					int[] rgb = getRGB(colorDat, format);
					int r = rgb[0];
					int g = rgb[1];
					int b = rgb[2];
					
					if(errorout == true && r == 0 && g == 0 && b == 0) {
						break;
					}
					
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. ("+e.getMessage()+")");
						// break out of the larger for-loop.
						errorout = true;
						break;
					}
					pxnum++;
					i += format.bytes;
					if(errorout == true) {
						break;
					}
				}
			}
			
			if(errorout == true) {
				if(pxnum >= width*height) {
					logger.log("Cont.: Received image data exceeds expected size by about "+(endOfImgDataOffset - i)+" bytes. (Is bit-depth mismatched?)");
				} else if(i >= endOfImgDataOffset) {
					logger.log("Cont.: Received image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
				} else {
					logger.log("Cont.: Unknown error. pxnum="+pxnum+"; width*height="+(width*height)+"; i="+i+"; data.length="+endOfImgDataOffset);
				}
				break;
			}
		}
		*/
		
		
		return image;
	}
	
	private static byte[] tgaDecode(byte[] data, byte[] decBuf, TGAPixelFormat format, int width, int height, int startingoffset, int endOfImgDataOffset) {
		int pxnum = 0;
		for(int i = startingoffset; i < endOfImgDataOffset && pxnum < width*height;) {
			boolean errorout = false;
			byte header = data[i];
			boolean rle = (header & 0x80) == 0x80; // Top bit is one
			int packlen = (header & 0x7F) + 1; // Bottom 15 bits plus one
			
			if(rle) {
				i += 1;
				byte[] colorDat = new byte[format.bytes];
				for(int k = 0; k < format.bytes; k++) {
					try {
						colorDat[k] = data[i+k];
					} catch(ArrayIndexOutOfBoundsException e) {
						colorDat[k] = 0;
						logger.log("Error: Reached end of image data while decoding colors of RLE packet. ("+e.getMessage()+")");
						// fill the rest of the colors with 0. draw pixels (unless all colors are zero). then break out of the larger for-loop.
						errorout = true;
						break;
					}
				}
				
				/*
				if(errorout == true && r == 0 && g == 0 && b == 0) {
					break;
				}
				*/
				
				// Repeat for as many times as are in packlen
				for(int pn = 0; pn < packlen; pn++) {
					for(int k = 0; k < format.bytes; k++) {
						// Maybe should double-check that we haven't overrun here
						try {
							decBuf[((pxnum+pn)*format.bytes)+k] = colorDat[k];
						} catch(ArrayIndexOutOfBoundsException e) {
							logger.log("Error: Reached end of image buffer while writing pixels of RLE packet. ("+e.getMessage()+")");
							// break out of the larger for-loop.
							errorout = true;
							break;
						}
					}
					if(errorout) {
						break; // lazy
					}
					//pxnum++;
				}
				pxnum += packlen;
				i += format.bytes;
			} else {
				i += 1;
				for(int j = 0; j < packlen; j++) {
					byte[] colorDat = new byte[format.bytes];
					for(int k = 0; k < format.bytes; k++) {
						try {
							colorDat[k] = data[i+k];
						} catch(ArrayIndexOutOfBoundsException e) {
							logger.log("Error: Reached end of image data while decoding pixels of RAW packet. ("+e.getMessage()+")");
							// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger for-loop.
							errorout = true;
							while(k < format.bytes) {
								colorDat[k] = 0;
								k++;
							}
							break;
						}
					}
					
					/*
					if(errorout == true && r == 0 && g == 0 && b == 0) {
						break;
					}
					*/
					
					for(int k = 0; k < format.bytes; k++) {
						try {
							decBuf[(pxnum*format.bytes)+k] = colorDat[k];
						} catch(ArrayIndexOutOfBoundsException e) {
							logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. ("+e.getMessage()+")");
							// break out of the larger for-loop.
							errorout = true;
							break;
						}
					}
					
					pxnum++;
					i += format.bytes;
					if(errorout == true) {
						break;
					}
				}
			}
			
			if(errorout == true) {
				if(pxnum >= width*height) {
					logger.log("Cont.: Received image data exceeds expected size by about "+(endOfImgDataOffset - i)+" bytes. (Is bit-depth mismatched?)");
				} else if(i >= endOfImgDataOffset) {
					logger.log("Cont.: Received image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
				} else {
					logger.log("Cont.: Unknown error. pxnum="+pxnum+"; width*height="+(width*height)+"; i="+i+"; data.length="+endOfImgDataOffset);
				}
				break;
			}
		}
		return decBuf;
	}
	
	private static BufferedImage tgaTranslate(byte[] decBuf, BufferedImage image, TGAPixelFormat format, int width, int height) {
		int pxnum = 0;
		for(int i = 0; i < (400*256*4) && pxnum < width*height;) {
			boolean errorout = false;
			
			// no headers; all RAW
			
			int[] colorDat = new int[format.bytes];
			
			for(int k = 0; k < format.bytes; k++) {
				try {
					colorDat[k] = decBuf[i+k] & 0xff;
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log("Error: Reached end of image data while decoding pixels of RAW packet. ("+e.getMessage()+")");
					// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger for-loop.
					errorout = true;
					while(k < format.bytes) {
						colorDat[k] = 0;
						k++;
					}
					break;
				}
			}
			int[] rgb = getRGB(colorDat, format);
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			
			if(errorout == true && r == 0 && g == 0 && b == 0) {
				break;
			}
			
			try {
				image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
			} catch(ArrayIndexOutOfBoundsException e) {
				logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. ("+e.getMessage()+")");
				// break out of the larger for-loop.
				errorout = true;
				break;
			}
			pxnum++;
			i += format.bytes;
			if(errorout == true) {
				break;
			}
			
			if(errorout == true) {
				//TODO: fix or rework
				/*
				if(pxnum >= width*height) {
					logger.log("Cont.: Received image data exceeds expected size by about "+(endOfImgDataOffset - i)+" bytes. (Is bit-depth mismatched?)");
				} else if(i >= endOfImgDataOffset) {
					logger.log("Cont.: Received image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
				} else {
					logger.log("Cont.: Unknown error. pxnum="+pxnum+"; width*height="+(width*height)+"; i="+i+"; data.length="+endOfImgDataOffset);
				}
				*/
				break;
			}
		}
		return image;
	}
	
	private static int[] getRGB(int[] bytes, TGAPixelFormat format) {
		int r=0,g=0,b=0;
		switch(format) {
			case RGB565:
				// GGGBBBBB RRRRRGGG
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 3) | ((bytes[0] & 0b11100000) >>> 5);
				b = bytes[0] & 0b00011111;
				// Scale from 5/6 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 2) | (g >>> 4);
				b = (b << 3) | (b >>> 2);
				break;
			case RGB5A1:
				// GGBBBBBA RRRRRGGG
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 2) | ((bytes[0] & 0b11000000) >>> 6);
				b = (bytes[0] & 0b00111110) >>> 1;
				// Scale from 5 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 3) | (g >>> 2);
				b = (b << 3) | (b >>> 2);
				break;
			case RGBA4: // untested
				// BBBBAAAA RRRRGGGG
				r = (bytes[0] & 0b11110000) >>> 4;
				g = bytes[0] & 0b00001111;
				b = bytes[1] & 0b00001111;
				// Scale from 4 bits to 8 bits
				r = (r << 4) | r;
				g = (g << 4) | g;
				b = (b << 4) | b;
				break;
			case RGB8:
				// BBBBBBBB GGGGGGGG RRRRRRRR
				r = bytes[2];
				g = bytes[1];
				b = bytes[0];
				break;
			case RGBA8: // untested
				// AAAAAAAA BBBBBBBB GGGGGGGG RRRRRRRR
				r = bytes[3];
				g = bytes[2];
				b = bytes[1];
				break;
		}
		return new int[] {r,g,b};
	}
}
