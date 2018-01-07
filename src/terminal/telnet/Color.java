package terminal.telnet;

import static java.lang.Math.max;

/** 8-bit Colors
 * @see https://en.wikipedia.org/wiki/ANSI_escape_code */
public class Color {

	private int value;
	public final String fg;
	public final String bg;
	
	public Color(int value) {
		this.value = value;
		fg = ((char)27) + "[38;5;" + value + 'm';
		bg = ((char)27) + "[48;5;" + value + 'm';
		
	}
	
	public Color(int r, int g, int b) {
		this(16 + 36 * r + 6 * g + b);
		if (r > 5) {
			throw new IllegalArgumentException("Invalid red: " + r);
		}
		if (g > 5) {
			throw new IllegalArgumentException("Invalid green: " + g);
		}
		if (b > 5) {
			throw new IllegalArgumentException("Invalid blue: " + b);
		}
	}
	
	public static Color greyscale(int value) {
		if (value > 23) {
			throw new IllegalArgumentException("Invalid greyscale: " + value);
		}
		return new Color(232 + value);
	}
	
	public double luminance() {
		if (value < 232) {
			int r = (value - 16) / 36;
			int g = (value - 16) % 36 / 6;
			int b = (value - 16) % 6;
			return max(max(r, g), b) / 6D;
		} else {
			return (value - 232) / 24D;
		}
	}
}
