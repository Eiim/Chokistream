package chokistream;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class IconLoader {
	
	public static final Image small16 = new Image("data:image/gif;base64,R0lGODlhEAAQAHAAACwAAAAAEAAQAIH////RsTntHCRWHwACNoQdqceQAmNqT4w74ETB4i9s3UdmgQOVYDiq4OO+bWzOMZveIUrvji07VFYbYSeiOTU4CuWhAAA7");
	public static final Image small32 = new Image("data:image/gif;base64,R0lGODlhIAAgAHAAACwAAAAAIAAgAIH////RsTntHCRWHwACi4SPFsubD08LoooZY7Pctoww1kCWpOeAy2i2Z5VCq+DWJRzIFG33eDLrCQc/CWU4vOQUO6SwuHImU02p7wKIWp+bLTfoteEqYbGSXHaNeWkT7th2d+M3RzUOY7LbSv0+XJQFl9ZnhAZYaPjnFBhyx5j4MLOo1qizQ0n0BmI02fG2xNnpiSGqMRGTUQAAOw==");
	public static final Image small48 = new Image("data:image/gif;base64,R0lGODlhMAAwAHAAACwAAAAAMAAwAIH////TqDftHCRWHwAC6YSPicHtCqNMrtowc7Si+29povOVpvCIW2MO7uuaqXqwHoznA8jQhi3QCXGeBo3xGSpfvFHgtozGOr0JsiPN7qiY1TOozXK7ims4zIVcwWdpkfwzt7XjxXd+TtfueDqKLNfnVxUo6PYXh2Uohri2yPhX+Lg0xjcZVal4SRlpuTmUyfYpFDqqVGpK2qmZSrTK2rrV6BmrJ9lqS4uLmCiaqtcbO1UVp7tZZwc1+qZm/AhMcbuI3Kz8DF0N28esseabx+309Q1p5ANEDvoGd4SuPcVD7LPnfVIyMx9NYt8pn9984cK/cwHZqSgAADs=");
	public static final Image small64 = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABABAMAAABYR2ztAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAMUExURf///9GxOe0cJFYfAFmmd6UAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADsSURBVEjH7ZZdDoQgDIThBrI3UG5A7383aflJkcKYbLJP2yfjfLaDQqsbIki4VYQeSDcJz/c/V46Trw7reZEbYuiXionwRY+UIxXimHWRGzISUr/rQpy6iCRQuhA6BScYdCZUCs8F6BFc5FBAfAJJAeyAaE7RapgJdAqfl0BkpiiA5aClaBXIjFpjVaHXyEC0gVSAlYVuYmlBTOw8VpdrC9XE90DYA2G3iLKMP/AWQK/6B58bbjm4afG2hwfnxdEDhxcef9xAYAuCTQy3QdhIYSvGzRyOAzxQ4EjCQw2PRTxY4Wh2eLg74/fAuRu/eQmwlppoFQAAAABJRU5ErkJggg==");
	
	public static void applyFavicon(Stage stage) {
		Image logo16 = get16x();
		Image logo32 = get32x();
		Image logo48 = get48x();
		Image logo64 = get64x();
		stage.getIcons().addAll(logo16, logo32, logo48, logo64);
	}
	
	public static Image get16x() {
		try {
			Image logo = new Image(IconLoader.class.getResourceAsStream("/res/logo16.png"));
			return logo;
		} catch (NullPointerException e) {
			return small16;
		}
	}
	
	public static Image get32x() {
		try {
			Image logo = new Image(IconLoader.class.getResourceAsStream("/res/logo32.png"));
			return logo;
		} catch (NullPointerException e) {
			return small32;
		}
	}
	
	public static Image get48x() {
		try {
			Image logo = new Image(IconLoader.class.getResourceAsStream("/res/logo48.png"));
			return logo;
		} catch (NullPointerException e) {
			return small48;
		}
	}
	
	public static Image get64x() {
		try {
			Image logo = new Image(IconLoader.class.getResourceAsStream("/res/logo64.png"));
			return logo;
		} catch (NullPointerException e) {
			return small64;
		}
	}
}
