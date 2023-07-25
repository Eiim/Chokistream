package chokistream;

import java.awt.image.BufferedImage;

import chokistream.props.ColorMode;

public class ColorHotfix {
	
	private static int hzModSwapRedBlue(int currentPixelColor) {
		//int newAlphaIdk = ((currentPixelColor&0b11111111000000000000000000000000) >>> 24);
		int newRedPixel = ((currentPixelColor&0b00000000000000000000000011111111)); //bitwise AND
		int newGrnPixel = ((currentPixelColor&0b00000000000000001111111100000000) >>> 8); //bit-shift right (unsigned)
		int newBluPixel = ((currentPixelColor&0b00000000111111110000000000000000) >>> 16);
		return 0xFF000000 +  ((newRedPixel)<<16) + ((newGrnPixel)<<8) + (newBluPixel);
	}
	
	private static int reverseEightBits(int a) {
		return (a&0b1)*128 + (a&0b10)*32 + (a&0b100)*8 + (a&0b1000)*2 + (a&0b10000)/2 + (a&0b100000)/8 + (a&0b1000000)/32 + (a&0b10000000)/128;
	}
	
	private static int makeGrayscale(int currentPixelColor) {
		int newRedPixel = ((currentPixelColor&0b00000000111111110000000000000000) >>> 16);
		int newGrnPixel = ((currentPixelColor&0b00000000000000001111111100000000) >>> 8);
		int newBluPixel = ((currentPixelColor&0b00000000000000000000000011111111));
		int newGrayPixel = (newRedPixel + newGrnPixel + newBluPixel)/3;
		return 0xFF000000 + ((newGrayPixel)<<16) + ((newGrayPixel)<<8) + (newGrayPixel);
	}
	
	// THIS IS BROKEN. IT IS BORDERLINE UNSOLVABLE. TRUST ME, I SPENT HOURS ON THIS. -C
	private static int vcBlueShift(int currentPixelColor) {
		//int newAlphaIdk = ((currentPixelColor&0b11111111000000000000000000000000) >>> 24);
		int newRedPixel = ((currentPixelColor&0b00000000111111110000000000000000) >>> 16);
		int newGrnPixel = ((currentPixelColor&0b00000000000000001111111100000000) >>> 8);
		int newBluPixel = ((currentPixelColor&0b00000000000000000000000011111111));
		
		// Formula: if Blue > 127, decrease proportional to how far from 128 it is.
		// If Blue < 128, *increase* proportional to how far it is.
		int newBluLevelCenter = 64;
		int maxBluFix = 48; // Don't go over 127 pwease :...3
		int maxBluPredictedOverflow = 2;
		if (newBluPixel < maxBluPredictedOverflow) {
			newBluPixel = (int)( newBluPixel + ((newBluPixel*1.0)/maxBluPredictedOverflow)*maxBluFix*2);
		} else if (newBluPixel > newBluLevelCenter) {
			newBluPixel = newBluPixel - (int)(((newBluPixel-newBluLevelCenter)/255.0)*maxBluFix);
		} else { //if (newBluPixel < newBluLevelCenter)
				newBluPixel = newBluPixel + (int)(((newBluLevelCenter-newBluPixel)/255.0)*maxBluFix);
		}
		//newBluPixel = (int)( (newBluPixel/2.0) );
		//newBluPixel = (int)((((newBluPixel-128.0)/128)*0x48)+newBluPixel);
		//
		//newBluPixel = (newBluPixel/16)*8 + newBluPixel/32;
		//
		//newBluPixel = newBluPixel%128 + 80;
		//
		//newBluPixel = ReverseEightBits(newBluPixel);
		//
		//newBluPixel = newBluPixel%128 + 64*((newBluPixel&0b10000000)>>>7);
		//
		//These two don't work vvv
		//newBluPixel = newBluPixel&0b111 + newBluPixel&0b10000000>>>4 + newBluPixel&0b1000000>>>1 + newBluPixel&0b100000>>>1 + newBluPixel&0b10000>>>1 + newBluPixel&0b1000<<4;
		//newBluPixel = (newBluPixel&0b111 + newBluPixel&0b10000000>>>3 + newBluPixel&0b1000000>>>1 + newBluPixel&0b100000>>>1 + newBluPixel&0b10000<<3 + newBluPixel&0b1000);
		//
		
		
		return 0xFF000000 + ((newRedPixel&0xFF)<<16) + ((newGrnPixel&0xFF)<<8) + (newBluPixel&0xFF);
	}
	/*
	 *
	 * Old logic for VcBlueShift
	 * 
	 * Guide to what's what... (What this code intends to do.)
	 * Letters ARGB stand for Alpha,Red,Green,Blue respectively.
	 * Each group of eight letters is a byte. Each letter is a Bit.
	 * The letter indicates which of the four the Bit contributes value to.
	 *
	 * What we receive (ARGB):AAAAAAAA RRRRRRRR GGGGGGGG  B  B  B  B B  BBB
	 *                                 |||||||| ||||||||  |  |  |  | ?
	 * How we (INTEND TO)              |||||||| ||||||||  |_ |_ |_ |_
	 * shift bits:                     |||||||| ||||||||    |  |  |  |
	 *                                 VVVVVVVV VVVVVVVV  ? V  V  V  V
	 * What we draw (ARGB):   11111111 RRRRRRRR GGGGGGGG  B B  B  B  B  BBB
	 *
	 * currentPixelArgbBefore = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
	 *			
	 * Here's the magic. On second thought, this should definitely be optimized with bitwise-AND,OR,NOT,XOR,etc.
	 * 
	 * newBluPixel = ((int)((currentPixelArgbBefore%256)/16))*8 + (int)((currentPixelArgbBefore%256)/32);
	 * 
	 * newBluPixel = newBluPixel + ((int)(currentPixelArgbBefore/8)%2)*128;
	 * 
	 * newBluPixel = (int)((newBluPixel/256.0) * 0x48) + newBluPixel;
	 */
	
	
	
	/*
	 * Possible clients: "NTR","HzMod"... (Case Insensitive)
	 */
	public static BufferedImage doColorHotfix(BufferedImage hotfixImage, ColorMode colorMode, boolean swapRB) {
		try {
			int currentPixelColor = 0;
			int currentW = 0;
			int currentH = 0;
			while (currentH < hotfixImage.getHeight()) {
				while (currentW < hotfixImage.getWidth()) {
					currentPixelColor = hotfixImage.getRGB(currentW, currentH);
					//currentPixelColor = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
					
					// If the client is HzMod, then always swap Red and Blue first. Then proceed to extra processing.
					if (swapRB) {
						currentPixelColor = hzModSwapRedBlue(currentPixelColor);
					}
					
					if (colorMode == ColorMode.VC_BLUE_SHIFT) {
						currentPixelColor = vcBlueShift(currentPixelColor);
					} else if (colorMode == ColorMode.GRAYSCALE) {
						currentPixelColor = makeGrayscale(currentPixelColor);
					}
					hotfixImage.setRGB(currentW, currentH, currentPixelColor);
					currentW++;
				}
				currentW = 0;
				currentH++;
			}
			return hotfixImage;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return new BufferedImage(400, 240, BufferedImage.TYPE_INT_RGB);
		}
	}
	
}

