package terminal.telnet;

/** Enumeration of color codes */
public enum BasicColor {

	/** Resets color to standard light-on-dark */
	DEFAULT(0),
	
	/** Brightens the current foreground color */
	BRIGHT(1),

	UNDERLINE(4),
	
	BLINK(5),
	
	BLINK2(6),
	
	/** Varies; sometimes the default light-grey, sometimes actual black */
	BLACK(30),
	
	/** Red foreground color */
	RED(31),

	/** Green foreground color */
	GREEN(32),

	/** Yellow foreground color */
	YELLOW(33),

	/** Blue foreground color */
	BLUE(34),

	/** Purple foreground color */
	PURPLE(35),

	/** Cyan foreground color */
	CYAN(36),

	/** White foreground color */
	WHITE(37),

	BRIGHT_BG(5),
	BLACK_BG(40),
	RED_BG(41),
	GREEN_BG(42),
	YELLOW_BG(43),
	BLUE_BG(44),
	PINK_BG(45),
	CYAN_BG(46),
	WHITE_BG(47);
	
	/** The telnet code for this color */
	private String code;

	/**
	 * Private constructor
	 * @param code The telnet code for this color
	 */
	private BasicColor(String code) {
		this.code = code;
	}

	/**
	 * Private constructor
	 * @param code The telnet code for this color
	 */
	private BasicColor(int code) {
		this.code = Integer.toString(code);
	}
	
	/**
	 * @return The telnet code for this color
	 */
	@Override
	public String toString() {
		return ((char)27) + "[" + code + 'm';
	}
}
