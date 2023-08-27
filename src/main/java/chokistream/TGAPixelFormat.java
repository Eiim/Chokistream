package chokistream;

import java.util.NoSuchElementException;

public enum TGAPixelFormat {
	RGBA8(4),
	RGB8(3),
	RGB565(2),
	RGB5A1(2),
	RGBA4(2);
	
	public int bytes;
	
	private TGAPixelFormat(int b) {
		bytes = b;
	}
	
	/**
	 * Get pixel format corresponding to HzMod/ChirunoMod code
	 * @param pf HzMod/ChirunoMod integer code
	 * @return corresponding TGA pixel format
	 */
	public static TGAPixelFormat fromInt(int pf) {
		switch(pf) {
			case 0:
				return RGBA8;
			case 1:
				return RGB8;
			case 2:
				return RGB565;
			case 3:
				return RGB5A1;
			case 4:
				return RGBA4;
			default:
				// Unknown code. Throw exception, because if we try for a fallback it will probably just make the inevitable crash harder to debug.
				throw new NoSuchElementException("TGA format code "+pf+" not recognized");
		}
	}
	
	/**
	 * Get HzMod/ChirunoMod integer code for a certain TGA format
	 * @return int HzMod/ChirunoMod integer code
	 */
	public int toInt() {
		switch(this) {
			case RGBA8:
				return 0;
			case RGB8:
				return 1;
			case RGB565:
				return 2;
			case RGB5A1:
				return 3;
			case RGBA4:
				return 4;
			default:
				return -1;
		}
	}
	
	
}
