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
	public static BufferedImage parseBytes(byte[] data, DSScreen screen) {
		int width = 240;
		int height = screen == DSScreen.BOTTOM ? 320 : 400;;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int pxnum = 0;
		for(int i = 22; i < data.length-26 && pxnum < width*height;) {
			byte header = data[i];
			boolean rle = (header & 0x80) == 0x80; // Top bit is one
			int packlen = (header & 0x7F) + 1; // Bottom 15 bits plus one
			
			if(rle) {
				// Two color bytes in LE order
				int b2 = data[i+2] & 0xff;
				int b1 = data[i+1] & 0xff;
				
				// RRRRRGGG GGBBBBBA
				int r = (b2 >>> 3);
				int g = ((b2 & 0b00000111) << 2) | ((b1 & 0b11000000) >>> 6);
				int b = (b1 & 0b00111110) >>> 1;
				
				// Scale from 5 bits to 8 bits
				r = (r << 3) | (r >>> 2);
				g = (g << 3) | (g >>> 2);
				b = (b << 3) | (b >>> 2);
				
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
				i += 3;
			} else {
				for(int j = 0; j < packlen; j++) {
					// Maybe should check we don't exceed our limit here
					int b2 = data[i+2+2*j] & 0xff;
					int b1 = data[i+1+2*j] & 0xff;
					
					// RRRRRGGG GGBBBBBA
					int r = (b2 & 0b11111000) >>> 3;
					int g = ((b2 & 0b00000111) << 2) | ((b1 & 0b11000000) >>> 6);
					int b = (b1 & 0b00111110) >>> 1;
					// Scale from 5 bits to 8 bits
					r = (r << 3) | (r >>> 2);
					g = (g << 3) | (g >>> 2);
					b = (b << 3) | (b >>> 2);
					
					try {
						image.setRGB(pxnum%width, pxnum/width, (r << 16) | (g << 8) | b);
					} catch(ArrayIndexOutOfBoundsException e) {
						// TODO: error handling?
					}
					pxnum++;
				}
				
				i += 1 + packlen*2;
			}
		}
		return image;
	}
}
