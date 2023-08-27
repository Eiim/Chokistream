package chokistream;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/*
 * Standalone helper class to convert a subset of SVG path curve data to a custom format used in LogoIcon.java
 * Not called in Chokistream, use as a standalone utility
 */
public class VectorConverter {

	public static void main(String[] args) {
		// must be even length
		String in = "";
		byte[] out = new byte[in.length()/2];
		
		Map<Character, Integer> map = new HashMap<>();
		map.put('0', 0);
		map.put('1', 1);
		map.put('2', 2);
		map.put('3', 3);
		map.put('4', 4);
		map.put('5', 5);
		map.put('6', 6);
		map.put('7', 7);
		map.put('8', 8);
		map.put('9', 9);
		map.put('.', 10);
		map.put(' ', 11);
		map.put('M', 12);
		map.put('C', 13);
		map.put('L', 14);
		map.put('Z', 15);
		
		for(int i = 0; i < out.length; i++) {
			int a = map.get(in.charAt(i*2));
			int b = map.get(in.charAt(i*2+1));
			out[i] = (byte) ((a << 4) + b);
		}
		System.out.println(Base64.getEncoder().encodeToString(out));
	}

}
