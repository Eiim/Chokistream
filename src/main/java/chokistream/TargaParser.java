package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.DSScreen;
import chokistream.props.LogLevel;

public class TargaParser {
	
	private static final Logger logger = Logger.INSTANCE;
	
	public static BufferedImage parseBytes(byte[] data, DSScreen screen, TGAPixelFormat format) {
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
		
		// Targa images from HzMod always have this set to 0
		int attrbits = (data[17] & 0xff) & 0b00001111;
		logger.log("attrbits="+(attrbits), LogLevel.EXTREME);
		
		int tgaBpp = data[16] & 0xff;
		boolean isMalformed24bpp = false;
		TGAPixelFormat tgaReportedFormat = switch(tgaBpp) {
			case 16 -> TGAPixelFormat.RGB5A1;
			case 17 -> TGAPixelFormat.RGB565;
			case 18 -> TGAPixelFormat.RGBA4;
			case 24 -> TGAPixelFormat.RGB8;
			case 32 -> TGAPixelFormat.RGBA8;
			case 8 -> {
				logger.log("Warning: Bit-depth \"BPP=8\" specified in Targa metadata. Function not implemented. (This error is common and can be safely ignored)", LogLevel.VERBOSE);
				yield format; // Fallback
			}
			default -> {
				logger.log("Warning: Invalid bit-depth \"BPP="+tgaBpp+"\" specified in Targa metadata. Falling back to "+format+" ...");
				yield format; // Fallback
			}
		};
		
		// iirc log output is bugged, but otherwise this seems to work as intended
		if(tgaReportedFormat != format) {
			if(tgaReportedFormat == TGAPixelFormat.RGBA8 && format == TGAPixelFormat.RGB8) {
				logger.log("Warning: Mode-Set packet reports RGB8 (24bpp) format, but Targa image is encoded as RGBA8 (32bpp). This is most likely caused by a known issue with HzMod; quietly fixing...", LogLevel.EXTREME);
				isMalformed24bpp = true;
			} else {
				logger.log("Warning: Color format specified in Targa metadata ("+tgaReportedFormat+") differs from format specified by Mode-Set packet ("+format+")");
			}
		}
		
		logger.log("format="+tgaReportedFormat, LogLevel.EXTREME);
		
		int idFieldLength = data[0] & 0xff;
		int startOfImgDataOffset = 18 + idFieldLength; // This should be correct... Formerly hardcoded to 22.
		logger.log("idFieldLength="+(idFieldLength), LogLevel.EXTREME);
		int endOfImgDataOffset = data.length - 26; // footer (26 bytes) + other areas (which probably aren't present)
		
		byte[] decBuf = new byte[400 * 256 * 4]; // middle-man decode buffer
		decBuf = tgaDecode(data, decBuf, tgaReportedFormat, width, height, startOfImgDataOffset, endOfImgDataOffset);
		
		// In this case, we want to do tgaDecode as RGBA8 but tgaTranslate as RGB8, apparently?
		if(isMalformed24bpp) {
			tgaReportedFormat = TGAPixelFormat.RGB8;
		}
		
		image = tgaTranslate(decBuf, image, tgaReportedFormat, width, height);
		
		return image;
	}
	
	private static byte[] tgaDecode(byte[] data, byte[] decBuf, TGAPixelFormat format, int width, int height, int startOfImgDataOffset, int endOfImgDataOffset) {
		boolean errorBreak = false;
		boolean errorEndOfInput = false;
		boolean errorEndOfDecbuf = false;
		int pxnum = 0;
		int i = startOfImgDataOffset;
		while(i < endOfImgDataOffset && pxnum < width*height) {
			byte header = data[i];
			boolean rle = (header & 0x80) == 0x80; // Top bit is one
			int packlen = (header & 0x7F) + 1; // Bottom 15 bits plus one
			
			i += 1;
			if(rle) {
				byte[] colorDat = new byte[format.bytes];
				for(int k = 0; k < format.bytes; k++) {
					try {
						colorDat[k] = data[i+k];
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.log("Error: Reached end of image data while decoding colors of RLE packet. [tgaDecode()] ("+e.getMessage()+")");
						errorEndOfInput = true;
						// fill the rest of the colors with 0. draw pixels (unless all colors are zero). then break out of the larger for-loop.
						if(k == 0)
							errorBreak = true;
						while(k < format.bytes) {
							colorDat[k] = 0;
							k++;
						}
						break;
					}
				}
				
				if(!errorBreak) {
				// Repeat for as many times as are in packlen
					for(int j = 0; j < packlen; j++) {
						for(int k = 0; k < format.bytes; k++) {
							// Maybe should double-check that we haven't overrun here
							try {
								decBuf[((pxnum+j)*format.bytes)+k] = colorDat[k];
							} catch(ArrayIndexOutOfBoundsException e) {
								logger.log("Error: Reached end of image buffer while writing pixels of RLE packet. [tgaDecode()] ("+e.getMessage()+")");
								// break out of the larger for-loop.
								errorBreak = true;
								errorEndOfDecbuf = true;
								break;
							}
						}
						if(errorBreak) {
							pxnum += j;
							break;
						}
					}
				}
				
				if(errorEndOfInput)
					errorBreak = true;
				
				if(!errorBreak) {
					pxnum += packlen;
					i += format.bytes;
				}
			} else {
				for(int j = 0; j < packlen; j++) {
					byte[] colorDat = new byte[format.bytes];
					for(int k = 0; k < format.bytes; k++) {
						try {
							colorDat[k] = data[i+k];
						} catch(ArrayIndexOutOfBoundsException e) {
							logger.log("Error: Reached end of image data while decoding pixels of RAW packet. [tgaDecode()] ("+e.getMessage()+")");
							// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger for-loop.
							errorEndOfInput = true;
							if(k == 0)
								errorBreak = true;
							while(k < format.bytes) {
								colorDat[k] = 0;
								k++;
							}
							break;
						}
					}
					
					if(errorBreak)
						break;
					
					for(int k = 0; k < format.bytes; k++) {
						try {
							decBuf[(pxnum*format.bytes)+k] = colorDat[k];
						} catch(ArrayIndexOutOfBoundsException e) {
							logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. [tgaDecode()] ("+e.getMessage()+")");
							// break out of the larger for-loop.
							errorBreak = true;
							errorEndOfDecbuf = true;
							break;
						}
					}
					
					if(errorEndOfInput)
						errorBreak = true;
					if(errorBreak)
						break;
					
					pxnum++;
					i += format.bytes;
				}
			}
		}
		
		if(errorBreak) {
			if(errorEndOfInput) {
				logger.log("Cont.: Received image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
			}
			if(errorEndOfDecbuf) {
				logger.log("Cont.: Received image data exceeds expected size by about "+(endOfImgDataOffset - i)+" bytes. (Is bit-depth mismatched?");
			}
		}
		
		return decBuf;
	}
	
	private static BufferedImage tgaTranslate(byte[] decBuf, BufferedImage image, TGAPixelFormat format, int width, int height) {
		boolean errorBreak = false;
		boolean errorEndOfInput = false;
		boolean errorEndOfOutputBuffer = false;
		int pxnum = 0;
		int i = 0;
		while(i < 400*256*4 && pxnum < width*height) {
			
			// no headers; all RAW
			
			int[] colorDat = new int[format.bytes];
			
			for(int k = 0; k < format.bytes; k++) {
				try {
					colorDat[k] = decBuf[i+k] & 0xff;
				} catch(ArrayIndexOutOfBoundsException e) {
					logger.log("Error: Reached end of image data while decoding pixels of RAW packet. [tgaTranslate()] ("+e.getMessage()+")");
					// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger for-loop.
					errorEndOfInput = true;
					if(k == 0) {
						errorBreak = true;
					}
					while(k < format.bytes) {
						colorDat[k] = 0;
						k++;
					}
					break;
				}
			}
			
			if(errorBreak) {
				break;
			}
			
			int[] rgb = getRGB(colorDat, format);
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			
			try {
				image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
			} catch(ArrayIndexOutOfBoundsException e) {
				logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. [tgaTranslate()] ("+e.getMessage()+")");
				// break out of the larger for-loop.
				errorBreak = true;
				errorEndOfOutputBuffer = true;
			}
			
			if(errorEndOfInput)
				break;
			if(errorBreak)
				break;
			
			pxnum++;
			i += format.bytes;
		}
		
		if(errorBreak) {
			if(errorEndOfInput) {
				logger.log("Cont.: Decoded image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
			}
			if(errorEndOfOutputBuffer) {
				logger.log("Cont.: Decoded image data exceeds expected size by about "+(width*height*format.bytes - i)+" bytes. (Is bit-depth mismatched?)");
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
