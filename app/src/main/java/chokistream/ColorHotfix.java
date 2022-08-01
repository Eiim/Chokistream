package chokistream;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class ColorHotfix {
	
	private static int HzModSwapRedBlue(int currentPixelColor) {
		int newAlphaIdk = ((currentPixelColor&0b11111111000000000000000000000000) >>> 24);
		int newRedPixel = ((currentPixelColor&0b00000000000000000000000011111111)); //bitwise AND
		int newGrnPixel = ((currentPixelColor&0b00000000000000001111111100000000) >>> 8); //bit-shift right (unsigned)
		int newBluPixel = ((currentPixelColor&0b00000000111111110000000000000000) >>> 16);
		currentPixelColor = ((newAlphaIdk << 24)) +  ((newRedPixel)<<16) + ((newGrnPixel)<<8) + (newBluPixel);
		return currentPixelColor;
	}
	
	private static int VcBlueShift(int currentPixelColor) {
		int newAlphaIdk = ((currentPixelColor&0b11111111000000000000000000000000) >>> 24);
		int newRedPixel = ((currentPixelColor&0b00000000111111110000000000000000) >>> 16);
		int newGrnPixel = ((currentPixelColor&0b00000000000000001111111100000000) >>> 8);
		//int newBluPixel = ((currentPixelColor&0b00000000000000000000000011111111));
		int newBluPixel = newAlphaIdk; // Test and Placeholder
		currentPixelColor = 0xFF000000 + ((newRedPixel&0xFF)<<16) + ((newGrnPixel&0xFF)<<8) + (newBluPixel&0xFF);
		return currentPixelColor;
	}
	/*
	 *
	 * Logic for VcBlueShift
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
	 *
	 * currentPixelArgbAfter = ((int)(currentPixelArgbBefore/256))*256 + (newBluPixel%256);
	 */
	
	
	
	/*
	 * Possible clients: "NTR","HzMod"... (Case Insensitive)
	 */
	public static Image DoColorHotfix(Image hotfixImageInput, ColorMode colorMode, Mod whichClient) {
		try {
			WritableImage hotfixImageWritable = new WritableImage( (int)hotfixImageInput.getWidth() , (int)hotfixImageInput.getHeight() );
			int currentPixelColor = 0;
			int currentW = 0;
			int currentH = 0;
			while (currentH < hotfixImageInput.getHeight()) {
				while (currentW < hotfixImageInput.getWidth()) {
					currentPixelColor = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
					
					// If the client is HzMod, then always swap Red and Blue first. Then proceed to extra processing.
					if (whichClient == Mod.HZMOD) {
						currentPixelColor = HzModSwapRedBlue(currentPixelColor);
					}
					
					if (colorMode == ColorMode.VC_BLUE_SHIFT) {
						currentPixelColor = VcBlueShift(currentPixelColor);
					}
					
					hotfixImageWritable.getPixelWriter().setArgb(currentW,currentH,currentPixelColor);
					currentW++;
				}
				currentW = 0;
				currentH++;
			}
			Image hotfixImageOutput = hotfixImageWritable;
			return hotfixImageOutput; // This shouldn't error...
		} catch (NullPointerException e) {
			e.printStackTrace();
			return new WritableImage(1,1);
		}
	}
	
}

