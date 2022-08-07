package chokistream;

import org.jcodec.common.Codec;
import org.jcodec.common.Format;

public enum VideoFormat implements EnumProp {
	PRORES("ProRes", Codec.PRORES, Format.MOV, "mov"),
	VP8("VP8", Codec.VP8, Format.MKV, "mkv");
	
	private final String longName;
	private final Codec codec;
	private final Format format;
	private final String extension;
	
	private VideoFormat(String name, Codec codec, Format format, String extension) {
		longName = name;
		this.codec = codec;
		this.format = format;
		this.extension = extension;
	}

	@Override
	public String getLongName() {
		return longName;
	}
	
	public Codec getCodec() {
		return codec;
	}
	
	public Format getFormat() {
		return format;
	}
	
	public String getExtension() {
		return extension;
	}
}
