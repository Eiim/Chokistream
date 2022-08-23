package chokistream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import chokistream.props.DSScreen;

public class TargaParser {
	public static BufferedImage parseBytes(byte[] data, DSScreen screen, TGAPixelFormat format) {
		int width = 240;
		int height = screen == DSScreen.BOTTOM ? 320 : 400;;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int pxnum = 0;
		for(int i = 22; i < data.length-26 && pxnum < width*height;) {
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
						Logger.INSTANCE.log(e.getMessage());
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
							Logger.INSTANCE.log(e.getMessage());
						}
					}
					int[] rgb = getRGB(colorDat, format);
					int r = rgb[0];
					int g = rgb[1];
					int b = rgb[2];
					
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						// TODO: error handling?
					}
					pxnum++;
					i += format.bytes;
				}
			}
		}
		return image;
	}
	
	private static int[] getRGB(int[] bytes, TGAPixelFormat format) {
		int r=0,g=0,b=0;
		switch(format) {
			case BITS_16A:
				// RRRRRGGG GGGBBBBB
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 3) | ((bytes[0] & 0b11100000) >>> 5);
				b = bytes[0] & 0b00011111;
				// Scale from 5/6 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 2) | (g >>> 4);
				b = (b << 3) | (b >>> 2);
				break;
			case BITS_16B:
				// RRRRRGGG GGBBBBBA
				r = (bytes[1] & 0b11111000) >>> 3;
				g = ((bytes[1] & 0b00000111) << 2) | ((bytes[0] & 0b11000000) >>> 6);
				b = (bytes[0] & 0b00111110) >>> 1;
				// Scale from 5 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 3) | (g >>> 2);
				b = (b << 3) | (b >>> 2);
				break;
			case BITS_16C:
				// AAAABBBB GGGGRRRR
				r = bytes[0] & 0b00001111;
				g = (bytes[0] & 0b11110000) >>> 4;
				b = bytes[1] & 0b00001111;
				// Scale from 4 bits to 8 bits
				r = (r << 4) | r;
				g = (g << 4) | g;
				b = (b << 4) | b;
				break;
			case BITS_24:
				// RRRRRRRRR GGGGGGGG BBBBBBBB
				r = bytes[2];
				g = bytes[1];
				b = bytes[0];
				break;
			case BITS_32:
				// AAAAAAAA BBBBBBBB GGGGGGGG RRRRRRRR
				r = bytes[0];
				g = bytes[1];
				b = bytes[2];
				break;
		}
		return new int[] {r,g,b};
	}
}
