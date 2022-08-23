package chokistream;

public enum TGAPixelFormat {
	BITS_32(4),
	BITS_24(3),
	BITS_16A(2),
	BITS_16B(2),
	BITS_16C(2);
	
	public int bytes;
	
	private TGAPixelFormat(int b) {
		bytes = b;
	}
	
	public static TGAPixelFormat fromInt(int pf) {
		switch(pf) {
			case 0:
				return BITS_32;
			case 2:
				return BITS_16A;
			case 3:
				return BITS_16B;
			case 4:
				return BITS_16C;
			default:
				return BITS_24;
		}
	}
}
