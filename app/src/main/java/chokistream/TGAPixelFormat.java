package chokistream;

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
	
	public static TGAPixelFormat fromInt(int pf) {
		switch(pf) {
			case 0:
				return RGBA8;
			case 2:
				return RGB565;
			case 3:
				return RGB5A1;
			case 4:
				return RGBA4;
			default:
				return RGB8;
		}
	}
}
