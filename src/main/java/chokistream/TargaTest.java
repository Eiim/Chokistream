package chokistream;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import chokistream.props.DSScreen;
import chokistream.props.LogLevel;
import chokistream.props.LogMode;

public class TargaTest {

	public static void main(String[] args) throws IOException {
		Logger.INSTANCE.init(LogMode.CONSOLE, LogLevel.EXTREME, "");
		Scanner s = new Scanner(System.in);
		FileInputStream fis = new FileInputStream(s.nextLine());
		s.close();
		BufferedImage im = TargaParser.parseBytes(fis.readAllBytes(), DSScreen.BOTTOM, TGAPixelFormat.RGB8);
		JFrame f = new JFrame();
		JLabel l = new JLabel(new ImageIcon(im));
		f.add(l);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.toFront();
	}

}
