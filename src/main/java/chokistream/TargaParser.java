package chokistream;

import java.awt.image.BufferedImage;
import java.util.Arrays;

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
		
		boolean doColorMapDecode = false;
		boolean colorMapPresent = (data[1] != 0x00);
		byte imageType = data[2];
		if(colorMapPresent) {
			switch(imageType) {
				case 1:
				//case 9:
					doColorMapDecode = true;
					break;
				default:
					doColorMapDecode = false;
					// todo: warning
					break;
			}
		} else {
			if(data[2] != 0x0A)
				logger.log("Warning: reported image type is not BGR_RLE. Function not implemented. imagetype="+(data[2] & 0xff));
		}
		
		//int colorMapOrigin = (data[4] & 0xff) * 256 + (data[3] & 0xff);
		int colorMapOrigin = 0; // not spec-compliant
		int colorMapLength = (data[6] & 0xff) * 256 + (data[5] & 0xff);
		
		if(doColorMapDecode) {
			if((data[16] & 0xff) != 8)
				doColorMapDecode = false;
		}
		
		// Note: This value is retrieved and used in HZModClient.java / ChirunoModClient.java, not here.
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
				if(doColorMapDecode) {
					yield switch(data[7] & 0xFF) {
						case 16 -> TGAPixelFormat.RGB5A1;
						case 17 -> TGAPixelFormat.RGB565;
						case 18 -> TGAPixelFormat.RGBA4;
						case 24 -> TGAPixelFormat.RGB8;
						case 32 -> TGAPixelFormat.RGBA8;
						default -> TGAPixelFormat.RGB5A1; // whatever
					};
				} else {
					logger.log("Warning: Bit-depth \"BPP=8\" specified in Targa metadata. Function not implemented. (This error is common and can be safely ignored)", LogLevel.VERBOSE);
					yield format; // Fallback
				}
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
		logger.log("idFieldLength="+(idFieldLength), LogLevel.EXTREME);
		
		// Account for Header (18 bytes + variable-length "ID" Field)
		int startOfImgData = 18 + idFieldLength;
		
		/* Footer should be 26 bytes */
		int footerOffs = data.length - 26;
		
		if(doColorMapDecode) {
			int colorMapOffs = startOfImgData;
			startOfImgData = tgaReportedFormat.bytes * colorMapLength;
			image = tgaDecodeRawColorMappedImg(data, image, tgaReportedFormat, width, height, startOfImgData, colorMapOffs, colorMapLength);
		} else {
			byte[] decBuf = new byte[400 * 256 * 4]; // middle-man decode buffer
			decBuf = tgaDecode(data, decBuf, tgaReportedFormat, width, height, startOfImgData, footerOffs);
			
			// Workaround known HzMod bug; 3DS-side, 24bpp images are encoded as if they were 32bpp. Refer to docs.
			if(isMalformed24bpp) {
				tgaReportedFormat = TGAPixelFormat.RGB8;
			}
			
			image = tgaTranslate(decBuf, image, tgaReportedFormat, width, height);
		}
		return image;
	}
	
	private static byte[] tgaDecode(byte[] data, byte[] decBuf, TGAPixelFormat format, int width, int height, int startOfImgData, int endOfImgData) {
		if(endOfImgData >= data.length || startOfImgData < 0) {
			throw new IllegalArgumentException("Data boundaries of "+startOfImgData+"-"+endOfImgData+" don't make sense for data with length "+data.length+"!");
		}
		
		boolean errorEndOfInput = false;
		boolean errorEndOfDecbuf = false;
		int pxnum = 0;
		int i = startOfImgData;
		
		packetloop:
		while(i < endOfImgData && pxnum < width*height && !errorEndOfInput && !errorEndOfDecbuf) { // do we need both of the first two checks?
			byte header = data[i];
			boolean rle = (header & 0b10000000) > 0; // Top bit is one
			int packlen = (header & 0b01111111) + 1; // Bottom 7 bits plus one
			i += 1;
			
			if(rle) {
				byte[] colorDat = Arrays.copyOfRange(data, i, i+format.bytes); // Automatically fills any extra positions with 0
				if(i+format.bytes > endOfImgData) {
					logger.log("Error: Reached end of image data while decoding colors of RLE packet. [tgaDecode()]");
					errorEndOfInput = true; // Will want to break out of the loop later since we hit the end, but first encode the pixels
				}
				
				// Repeat for each pixel we're encoding
				for(int j = 0; j < packlen; j++) {
					if((pxnum+j+1)*format.bytes > decBuf.length) {
						// We're going to overrun. Copy as much as possible, although this will result in broken color data
						System.arraycopy(colorDat, 0, decBuf, (pxnum+j)*format.bytes, decBuf.length-(pxnum+j)*format.bytes-1);
						logger.log("Error: Reached end of image buffer while writing pixels of RLE packet. [tgaDecode()] ("+((pxnum+j+1)*format.bytes)+">"+decBuf.length+")");
						// break out of the larger while-loop.
						pxnum += j;
						errorEndOfDecbuf = true;
						break packetloop;
					} else {
						// We know it won't overrun here
						System.arraycopy(colorDat, 0, decBuf, (pxnum+j)*format.bytes, format.bytes);
					}
				}
				
				pxnum += packlen;
				i += format.bytes;
			} else {
				int bytelen = packlen*format.bytes;
				
				if(i+bytelen > endOfImgData) {
					logger.log("Error: Reached end of image data while decoding pixels of RAW packet. [tgaDecode()] (Attempted to read "+bytelen+" bytes starting at "+i+" but data is only "+endOfImgData+" bytes long)");
					// fill the rest of the colors with 0. draw this pixel (unless all colors are zero). then break out of the larger while-loop.
					bytelen = endOfImgData-i-1;
					errorEndOfInput = true;
				}
				
				if((pxnum*format.bytes)+bytelen >= decBuf.length) {
					logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. [tgaDecode()] (Attempted to write "+bytelen+" bytes starting at "+pxnum*format.bytes+" but buffer is only "+decBuf.length+" bytes long)");
					// Write as much data as possible
					bytelen = decBuf.length - (pxnum*format.bytes) - 1;
					errorEndOfDecbuf = true;
				}
				
				// Should all be safe now so that we can't get an exception here
				System.arraycopy(data, i, decBuf, pxnum*format.bytes, bytelen);
				
				// Fill any extra 0s here
				if(bytelen%format.bytes != 0) {
					for(int j = 0; j < format.bytes-(bytelen%format.bytes); j++) {
						decBuf[(pxnum*format.bytes)+bytelen+j] = 0;
					}
				}
				
				i += bytelen;
				pxnum += bytelen/format.bytes;
			}
		}
		
		if(errorEndOfInput) {
			logger.log("Cont.: Received image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
		}
		if(errorEndOfDecbuf) {
			logger.log("Cont.: Received image data exceeds expected size by about "+(endOfImgData - i)+" bytes. (Is bit-depth mismatched?)");
		}
			
		return decBuf;
	}
	
	private static BufferedImage tgaTranslate(byte[] decBuf, BufferedImage image, TGAPixelFormat format, int width, int height) {
		boolean errorEndOfInput = false;
		boolean errorEndOfOutputBuffer = false;
		int pxnum = 0;
		int i = 0;
		
		while(pxnum < width*height && !errorEndOfInput) {
			// no headers; all RAW
			
			byte[] colorDat = Arrays.copyOfRange(decBuf, i, i+format.bytes); // Extra 0s automatically appended if needed
			if(i+format.bytes >= decBuf.length) {
				logger.log("Error: Reached end of image data while decoding pixels of RAW packet. [tgaTranslate()] (Attempted to read "+format.bytes+" bytes starting at "+i+" but image data is "+decBuf.length+" bytes long)");
				errorEndOfInput = true; // Exceeded length of input data, break out of larger loop but still write
			}
			
			try {
				image.setRGB(pxnum%width, pxnum/width, getRGB(colorDat, format));
			} catch(ArrayIndexOutOfBoundsException e) {
				logger.log("Error: Reached end of image buffer while writing pixels of RAW packet. [tgaTranslate()] ("+e.getMessage()+")");
				// break out of the larger for-loop.
				errorEndOfOutputBuffer = true;
				break;
			}
			
			pxnum++;
			i += format.bytes;
		}
		
		if(errorEndOfInput) {
			logger.log("Cont.: Decoded image data is about "+(width*height - pxnum)+" pixels smaller than expected. (Is bit-depth mismatched?)");
		}
		if(errorEndOfOutputBuffer) {
			logger.log("Cont.: Decoded image data exceeds expected size by about "+(width*height*format.bytes - i)+" bytes. (Is bit-depth mismatched?)");
		}
		
		return image;
	}
	
	private static BufferedImage tgaDecodeRawColorMappedImg(byte[] data, BufferedImage image, TGAPixelFormat format, int width, int height, int imgDataOffs, int colorMapDataOffs, int colorMapLength) {
		
		//colorMapDataOffs += 4;
		
		int[] colorMap = new int[colorMapLength];
		for(int i = 0; i < colorMapLength; i++) {
			byte[] colorDat = Arrays.copyOfRange(data, i+colorMapDataOffs, i+colorMapDataOffs+format.bytes);
			colorMap[i] = getRGB(colorDat, format);
		}
		
		imgDataOffs += 18; // temp hack
		
		for(int pxnum = 0; pxnum < width*height; pxnum++) {
			try {
				int colorIndex = data[imgDataOffs+pxnum] & 0xFF;
				image.setRGB(pxnum%width, pxnum/width, colorMap[colorIndex]);
			} catch(Exception e) {
				//break;
			}
		}
		return image;
	}
	
	private static int getRGB(byte[] bytes, TGAPixelFormat format) {
		return switch(format) {
			case RGB565 -> {
				// GGGBBBBB RRRRRGGG
				int r = (bytes[1] & 0b11111000) >>> 3;
				int g = ((bytes[1] & 0b00000111) << 3) | ((bytes[0] & 0b11100000) >>> 5);
				int b = bytes[0] & 0b00011111;
				// Scale from 5/6 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 2) | (g >>> 4);
				b = (b << 3) | (b >>> 2);
				yield (r << 16) | (g << 8) | b;
			}
			case RGB5A1 -> {
				// GGBBBBBA RRRRRGGG
				int r = (bytes[1] & 0b11111000) >>> 3;
				int g = ((bytes[1] & 0b00000111) << 2) | ((bytes[0] & 0b11000000) >>> 6);
				int b = (bytes[0] & 0b00111110) >>> 1;
				// Scale from 5 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 3) | (g >>> 2);
				b = (b << 3) | (b >>> 2);
				yield (r << 16) | (g << 8) | b;
			}
			case RGBA4 -> { // untested
				// BBBBAAAA RRRRGGGG
				int r = (bytes[0] & 0b11110000) >>> 4;
				int g = bytes[0] & 0b00001111;
				int b = bytes[1] & 0b00001111;
				// Scale from 4 bits to 8 bits
				r = (r << 4) | r;
				g = (g << 4) | g;
				b = (b << 4) | b;
				yield (r << 16) | (g << 8) | b;
			}
			case RGB8 -> {
				// BBBBBBBB GGGGGGGG RRRRRRRR
				int r = bytes[2] & 0xff;
				int g = bytes[1] & 0xff;
				int b = bytes[0] & 0xff;
				yield (r << 16) | (g << 8) | b;
			}
			case RGBA8 -> { // untested
				// AAAAAAAA BBBBBBBB GGGGGGGG RRRRRRRR
				int r = bytes[3] & 0xff;
				int g = bytes[2] & 0xff;
				int b = bytes[1] & 0xff;
				yield (r << 16) | (g << 8) | b;
			}
		};
	}
}
