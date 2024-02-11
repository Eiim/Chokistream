package chokistream;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.MissingResourceException;

import javax.imageio.ImageIO;

public class IconLoader {
	
	private static final Logger logger = Logger.INSTANCE;
	
	private static Image get(ImRes res) {
		try {
			Image logo = ImageIO.read(IconLoader.class.getResourceAsStream(res.path));
			return logo;
		} catch (IllegalArgumentException e) {
			logger.logOnce("Couldn't find logo, probably not running from jar");
			return res.small;
		} catch (IOException e) {
			logger.log("Error reading favicon!");
			return res.small;
		}
	}
	
	public static Image get16x() {
		return get(ImRes.SIXTEEN);
	}
	
	public static Image get32x() {
		return get(ImRes.THIRTYTWO);
	}
	
	public static Image get48x() {
		return get(ImRes.FOURTYEIGHT);
	}
	
	public static Image get64x() {
		return get(ImRes.SIXTYFOUR);
	}
	
	public static List<Image> getAll() {
		ArrayList<Image> out = new ArrayList<>(4);
		out.add(get16x());
		out.add(get32x());
		out.add(get48x());
		out.add(get64x());
		return out;
	}
	
	private static enum ImRes {
		SIXTEEN("/res/logo16.png", "R0lGODlhEAAQAHAAACH5BAEAAAUALAAAAAAQABAAgtGxOe0cJFYfANKzPtGyPAAAAAAAAAAAAAM6WKrQvrC0QGuLM4gt6GWAxo3BF45oB0hUSpanS07yHNfqXcPtXrK4X2Ggsy0yLwABE6p4VhGQA7pIAAA7"),
		THIRTYTWO("/res/logo32.png", "R0lGODlhIAAgAHAAACH5BAEAAAMALAAAAAAgACAAgdGxOe0cJFYfAAAAAAKLnI8Gy5sPTwOhhhljs9y2jDCWQJak54DLaLZnlUJr4NYlDMgUbfd4MusJBT8JZTi85BQ7pLC4ciZTTanvMohan5stN+i14SphsZJcdo15aRPu2HZ34zdHNQ5jsttK/T5clAWX1meEBlho+OcUGHLHmPgws6jWqLNDSfQGYjTZ8bbE2emJIaoxEZNRAAA7"),
		FOURTYEIGHT("/res/logo48.png", "R0lGODlhMAAwAHAAACH5BAEAAAMALAAAAAAwADAAgdOoN+0cJFYfAAAAAALpnI+JwO0Ko0yuWjBztKH7b2mi85Vm8IhbYwru65qperAejOcCyNCGHdAJcZ4GjfEZKl+8EeC2jMY6vQmyI83uqJjVM6jNcruKazjMhVzBZ2mR/DO3tePFd35O1+54Ooos1+dXFSjo9heHZSiGuLbI+Ff4uDTGNxlVqXhJGWm5OZTJ9ikUOqpUakraqZlKtMrautXoGasn2WpLi4uYKJqq1xs7VRWnu1lnBzX6pmb8CExxu4jcrPwMXQ3bx6yx5pvH7fT1DWnkA0QO+gZ3hK49xUPss+d9UjIzH01i3ymf33zhwr9zAdmpKAAAOw=="),
		SIXTYFOUR("/res/logo64.png", "R0lGODlhQABAAHAAACH5BAEAAAMALAAAAABAAEAAgdGxOe0cJFYfAAAAAAL/nI+pCe3flpy0HohztLwbDWbeqITBiaYhOWbpC6MiW2HxjWO0BOHCDwzmHjtEDxZMKoExSPGYWkqXMCcLeppqqSpi6xHdipVdhwccHquZZUAHnV3Lf20OPDDPC+qUu17Px9OQ9jdXNuFAWGgos8GQGLf41+h4MYgiWXj4CICZqXliZnQZ+Zm3+UFqukhpqbo6GSoKiQcLGjD7agso6zZAuxuLGwEXzDsMZSxsU6q8RgnszIgcLf3cW209Bq2rLcbd2ew9Be45vlUufk6G3b3ORZ39LoT96z5P15saXotPj7zPnL98AO3dewfNlUB8rRQuXIcqoLpxDUfx6wexoit+ZwzrlSCF0VtEToqkjeR0UWTCPrRCBgvEEqQzmDEvujT1wosdP7ba+Dpz5+axdjSwTFRTReeVoEfJJRVVxCDIF1qGQI26ryWOrfF+YrXIlGtXr19RMruxomyNEGwrqbXQVsPbJ2zVFgAAOw==");
		
		private final String path;
		private final Image small;
		
		private ImRes(String path, String small) {
			this.path = path;
			try {
				this.small = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(small)));
			} catch(IOException e) {
				throw new MissingResourceException("Somehow failed to parse built-in gifs", "IconLoader", "smallgifs");
			}
		}
	}
}
