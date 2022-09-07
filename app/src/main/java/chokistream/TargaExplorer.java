package chokistream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import chokistream.props.DSScreen;

public class TargaExplorer {

	public static void main(String[] args) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\ethan\\Documents\\GitHub\\Chokistream\\odd.tga"));
		BufferedImage im = TargaParser.parseBytes(bytes, DSScreen.TOP, TGAPixelFormat.RGBA8);
		JLabel lab = new JLabel(new ImageIcon(im));
		JFrame frame = new JFrame();
		frame.add(lab);
		frame.pack();
		frame.setVisible(true);
	}

}
