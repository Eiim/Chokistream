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
	
	public static final Image small16;
	public static final Image small32;
	public static final Image small48;
	public static final Image small64;
	
	static {
		try {
			small16 = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode("R0lGODlhEAAQAHAAACH5BAEAAAUALAAAAAAQABAAgtGxOe0cJFYfANKzPtGyPAAAAAAAAAAAAAM6WKrQvrC0QGuLM4gt6GWAxo3BF45oB0hUSpanS07yHNfqXcPtXrK4X2Ggsy0yLwABE6p4VhGQA7pIAAA7")));
			small32 = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode("R0lGODlhIAAgAHAAACH5BAEAAAMALAAAAAAgACAAgdGxOe0cJFYfAAAAAAKLnI8Gy5sPTwOhhhljs9y2jDCWQJak54DLaLZnlUJr4NYlDMgUbfd4MusJBT8JZTi85BQ7pLC4ciZTTanvMohan5stN+i14SphsZJcdo15aRPu2HZ34zdHNQ5jsttK/T5clAWX1meEBlho+OcUGHLHmPgws6jWqLNDSfQGYjTZ8bbE2emJIaoxEZNRAAA7")));
			small48 = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode("R0lGODlhMAAwAHAAACH5BAEAAAMALAAAAAAwADAAgdOoN+0cJFYfAAAAAALpnI+JwO0Ko0yuWjBztKH7b2mi85Vm8IhbYwru65qperAejOcCyNCGHdAJcZ4GjfEZKl+8EeC2jMY6vQmyI83uqJjVM6jNcruKazjMhVzBZ2mR/DO3tePFd35O1+54Ooos1+dXFSjo9heHZSiGuLbI+Ff4uDTGNxlVqXhJGWm5OZTJ9ikUOqpUakraqZlKtMrautXoGasn2WpLi4uYKJqq1xs7VRWnu1lnBzX6pmb8CExxu4jcrPwMXQ3bx6yx5pvH7fT1DWnkA0QO+gZ3hK49xUPss+d9UjIzH01i3ymf33zhwr9zAdmpKAAAOw==")));
			small64 = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode("R0lGODlhQABAAHAAACH5BAEAAAMALAAAAABAAEAAgdGxOe0cJFYfAAAAAAL/nI+pCe3flpy0HohztLwbDWbeqITBiaYhOWbpC6MiW2HxjWO0BOHCDwzmHjtEDxZMKoExSPGYWkqXMCcLeppqqSpi6xHdipVdhwccHquZZUAHnV3Lf20OPDDPC+qUu17Px9OQ9jdXNuFAWGgos8GQGLf41+h4MYgiWXj4CICZqXliZnQZ+Zm3+UFqukhpqbo6GSoKiQcLGjD7agso6zZAuxuLGwEXzDsMZSxsU6q8RgnszIgcLf3cW209Bq2rLcbd2ew9Be45vlUufk6G3b3ORZ39LoT96z5P15saXotPj7zPnL98AO3dewfNlUB8rRQuXIcqoLpxDUfx6wexoit+ZwzrlSCF0VtEToqkjeR0UWTCPrRCBgvEEqQzmDEvujT1wosdP7ba+Dpz5+axdjSwTFRTReeVoEfJJRVVxCDIF1qGQI26ryWOrfF+YrXIlGtXr19RMruxomyNEGwrqbXQVsPbJ2zVFgAAOw==")));
		} catch(IOException e) {
			throw new MissingResourceException("Somehow failed to parse built-in gifs", "IconLoader", "smallgifs");
		}
	}
	
	public static final Logger logger = Logger.INSTANCE;
	
	public static Image get16x() {
		try {
			Image logo = ImageIO.read(IconLoader.class.getResourceAsStream("/res/logo16.png"));
			return logo;
		} catch (IllegalArgumentException e) {
			logger.logOnce("Couldn't find logo, probably not running from jar");
			return small16;
		} catch (IOException e) {
			logger.log("Error reading favicon!");
			return small16;
		}
	}
	
	public static Image get32x() {
		try {
			Image logo = ImageIO.read(IconLoader.class.getResourceAsStream("/res/logo32.png"));
			return logo;
		} catch (IllegalArgumentException e) {
			logger.logOnce("Couldn't find logo, probably not running from jar");
			return small32;
		} catch (IOException e) {
			logger.log("Error reading favicon!");
			return small32;
		}
	}
	
	public static Image get48x() {
		try {
			Image logo = ImageIO.read(IconLoader.class.getResourceAsStream("/res/logo48.png"));
			return logo;
		} catch (IllegalArgumentException e) {
			logger.logOnce("Couldn't find logo, probably not running from jar");
			return small48;
		} catch (IOException e) {
			logger.log("Error reading favicon!");
			return small48;
		}
	}
	
	public static Image get64x() {
		try {
			Image logo = ImageIO.read(IconLoader.class.getResourceAsStream("/res/logo64.png"));
			return logo;
		} catch (IllegalArgumentException e) {
			logger.logOnce("Couldn't find logo, probably not running from jar");
			return small64;
		} catch (IOException e) {
			logger.log("Error reading favicon!");
			return small64;
		}
	}
	
	public static List<Image> getAll() {
		ArrayList<Image> out = new ArrayList<>(4);
		out.add(get16x());
		out.add(get32x());
		out.add(get48x());
		out.add(get64x());
		return out;
	}
}
