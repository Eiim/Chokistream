package chokistream;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class ColorHotfix {

	public static Image HzModSwapRedBlue(Image hotfixImageInput) {
		WritableImage hotfixImageWritable = new WritableImage( (int)hotfixImageInput.getWidth() , (int)hotfixImageInput.getHeight() );
		Image hotfixImageOutput = null;
		int currentPixelArgbBefore = 0;
		int currentPixelArgbAfter = 0;
		int currentW = 0;
		int currentH = 0;
		while (currentH < hotfixImageInput.getHeight()) {
			while (currentW < hotfixImageInput.getWidth()) {
				currentPixelArgbBefore = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
				//int newAlphaIdk = (currentPixelArgbBefore/16777216);
				int newRedPixel = ((currentPixelArgbBefore&0b00000000000000000000000011111111)); //bitwise AND
				int newGrnPixel = ((currentPixelArgbBefore&0b00000000000000001111111100000000) >>> 8); //bit-shift right (unsigned)
				int newBluPixel = ((currentPixelArgbBefore&0b00000000111111110000000000000000) >>> 16);
				//if (newRedPixel == 255) {
				//	newRedPixel = 0;
				//}
				//if (newBluPixel == 255) {
				//	newBluPixel = 0;
				//}
				currentPixelArgbAfter = 0xFF000000 +  ((newRedPixel&0xFF)<<16) + ((newGrnPixel&0xFF)<<8) + (newBluPixel&0xFF);
				hotfixImageWritable.getPixelWriter().setArgb(currentW,currentH,currentPixelArgbAfter);
				currentW++;
			}
			currentW = 0;
			currentH++;
		}
		hotfixImageOutput = hotfixImageWritable;
		return hotfixImageOutput;
	}
	/**
	 * Attempt to hot-fix the colors if the option is enabled(?)
	 * This is specifically aimed at Virtual Console games:
	 * Most notably Pokemon Yellow, but also Super Mario Bros, etc.
	 * 
	 * Theoretically can handle images with arbitrary dimensions and resolutions.
	 * If I implement it right, the Pixel Format of
	 * the hotfixImageInput shouldn't matter.
	 * 
	 * Assumes the input image's dimensions are each not 0 (or negative or null).
	 */
	public static Image VcBlueShift(Image hotfixImageInput) {
		WritableImage hotfixImageWritable = new WritableImage( (int)hotfixImageInput.getWidth() , (int)hotfixImageInput.getHeight() );
		Image hotfixImageOutput = null;
		int currentPixelArgbBefore = 0;
		int currentPixelArgbAfter = 0;
		int currentW = 0; // Current Width (position of pixel being processed)
		int currentH = 0; // Current Height
		/** 
		 * Pixel processing and transferring loops. Note they are zero-indexed.
		 * Also note. JFX has functions to automatically transfer pieces of the image,
		 * But we can't use that, because we need to play with the bit order ),:
		 */
		while (currentH < hotfixImageInput.getHeight()) {
			while (currentW < hotfixImageInput.getWidth()) {
/**
 * Logic for hot-fixing image colors! I am the king of hexadecimal. -C
 * 
 * Guide to what's what... (What this code intends to do.)
 * Letters ARGB stand for Alpha,Red,Green,Blue respectively.
 * Each group of eight letters is a byte. Each letter is a Bit.
 * The letter indicates which of the four the Bit contributes value to.
 * 
 * ...Did I explain that badly? Was that overly confusing? Oh well... -C
 *
 * What we receive (ARGB):AAAAAAAA RRRRRRRR GGGGGGGG  B  B  B  B B  BBB
 *                                 |||||||| ||||||||  |  |  |  | ?
 * How we (INTEND TO)              |||||||| ||||||||  |_ |_ |_ |_
 * shift bits:                     |||||||| ||||||||    |  |  |  |
 *                                 VVVVVVVV VVVVVVVV  ? V  V  V  V
 * What we draw (ARGB):   11111111 RRRRRRRR GGGGGGGG  B B  B  B  B  BBB
 */
				currentPixelArgbBefore = hotfixImageInput.getPixelReader().getArgb(currentW, currentH);
				
				// Here's the magic. On second thought, this should definitely be optimized with bitwise-AND,OR,NOT,XOR,etc.
				int newBluPixel = (currentPixelArgbBefore%256);
//				newBluPixel = ((int)((currentPixelArgbBefore%256)/16))*8 + (int)((currentPixelArgbBefore%256)/32);
//				newBluPixel = newBluPixel + ((int)(currentPixelArgbBefore/8)%2)*128;
				
				
				
				newBluPixel = (int)((newBluPixel/256.0) * 0x48) + newBluPixel;
				
				currentPixelArgbAfter = ((int)(currentPixelArgbBefore/256))*256 + (newBluPixel%256);
				
				hotfixImageWritable.getPixelWriter().setArgb(currentW,currentH,currentPixelArgbAfter);
				currentW++;
			}
			currentW = 0;
			currentH++;
		}
		hotfixImageOutput = hotfixImageWritable;
		return hotfixImageOutput;
	}
	
}

