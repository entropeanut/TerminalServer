package terminal.telnet;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.lanterna.input.CharacterPattern;
import com.googlecode.lanterna.input.CharacterPattern.Matching;

import terminal.Protocol;
import terminal.Terminal;
import terminal.TerminalException;

/** A remote telnet terminal */
public class TelnetTerminal extends Terminal {

    /** the port on which to listen for incoming connections */
    public static final int DEFAULT_PORT = 23;

    /** Interpret As Command */
    public static final byte IAC = (byte) 255;
    
    /** Request or confirm expectation that the the receiver NOT do something */
    public static final byte DONT = (byte) 254;
    
    /** Request or confirm expectation that the the receiver do something */
    public static final byte DO = (byte) 253;

    /** Sender requests permission or confirms request to NOT do something */
    public static final byte WONT = (byte) 252;
    
    /** Sender requests permission or confirms request to do something */
    public static final byte WILL = (byte) 251;

    /** Sub-negotiation */
    private static final int SB = (byte) 250;

    /** Erase Line */
    public static final byte EL = (byte) 248;

    /** Erase Character */
    public static final byte EC = (byte) 247;
    
    /** Escape */
    public static final byte ESC = (byte) 27;

    /** Terminal Type (Name) */
    public static final byte TERMINAL_TYPE = (byte) 24;
    
    /** Negotiate about window size */
    public static final byte NAWS = (byte) 31;
    
    /** Terminal Speed (bit rate) */
    public static final byte SPEED = (byte) 32;
    
    /** Environment Variables */
    public static final byte NEW_ENVIRON = (byte) 39;
    
    /** End of Sub-negotiation */
    private static final int SE = (byte) 240;

    /** Echo */
    public static final byte ECHO = 1;
    
    /** Suppress Go Ahead */
    public static final byte SGA = 3;
    
    /** Backspace */
    public static final byte BS = 8;
    
    /** Carriage Return */
    public static final char CR = 13;
    
    /** Line Feed */
    public static final char LF = 10;
    
    /** Line Mode */
    public static final byte LM = 34;

    /** Left Bracket */
    public static final byte LEFT_BRACKET = 91;

    /** Cursor Right */
    public static final byte RIGHT = 67;
    
    /** Cursor Left */
    public static final byte LEFT = 68;
    
    /** Delete */
    public static final byte DELETE = 127;

    private static final Logger log = LoggerFactory.getLogger(TelnetTerminal.class);

    /** the network connection */
    private final SocketChannel socket;

    /** the observers that will be notified about client activity */
    private final Set<Listener> listeners = new HashSet<>();
    
    private boolean echo = false;
    
    /** the prompt that is displayed when ready to accept input */
    private String prompt = "";;

    /** determines whether the terminal will try to erase the prompt and any 
     * user input before printing new output, and then reprint the prompt at the
     * end  */
    private boolean movePrompt = false; 
    
    /** indicates whether the prompt is currently displayed on the last line of
     * the terminal window */
    private boolean promptDisplayed = false;

    /** the network input buffer */
    private ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);

    /** the current command */
    private StringBuilder data = new StringBuilder();
    
    /** the input cursor position */
    private int cursor = 0;

    private List<String> commandList = new LinkedList<String>() {
            public boolean add(String object) {
                if(this.size() == 50) {
                    super.removeFirst();
                }
                return super.add(object);
            }
        };
    /**
	 * Constructor
	 * @param socket the network connection
	 */
	public TelnetTerminal(SocketChannel socket) {
	    
		this.socket = socket;
		
		try {
//            socket.write(ByteBuffer.wrap(new byte[]{IAC, WILL, ECHO}));
//            socket.write(ByteBuffer.wrap(new byte[]{IAC, DONT, LM}));
            socket.write(ByteBuffer.wrap(new byte[]{IAC, DO, NAWS}));
		} catch (Exception e) {
		    throw new TerminalException(e);
		}
	}

    @Override
	public void print(String text) throws TerminalException {
        try {
            clearPrompt();
            socket.write(ByteBuffer.wrap(text.getBytes()));
        } catch (ClosedChannelException ignored) {
        } catch (IOException e) {
            throw new TerminalException(e);
        }
	}

    public void print(String text, BasicColor color) throws TerminalException {
        try {
            clearPrompt();
            String string = color.toString() + text.toString() + 
                BasicColor.DEFAULT.toString();
            socket.write(ByteBuffer.wrap(string.getBytes()));
        } catch (ClosedChannelException ignored) {
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }

	@Override
    public void printLine(String text) throws TerminalException  {
        try {
            clearPrompt();
            String string = text + CR + LF;
            socket.write(ByteBuffer.wrap(string.getBytes()));
        } catch (ClosedChannelException ignored) {
        } catch (IOException e) {
            throw new TerminalException(e);
        }
	}

    public void println(BasicColor color, String text) throws TerminalException  {
        try {
            clearPrompt();
            String string = color.toString() + text.toString() + 
                BasicColor.DEFAULT.toString() + CR + LF;
            socket.write(ByteBuffer.wrap(string.getBytes()));
        } catch (ClosedChannelException ignored) {
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }

    @Override
    public String readLine() throws TerminalException {
//        try {
//            prompt();
//            buffer.reset();
//             
//            for (;;) {
//                byte data = readByte();
//                if (data == IAC) {
//                    subnegotiate();
//                } else if (data == BS || data == DELETE || data == EC) {
//                    if (buffer.size() > 0) {
//                        byte[] byteArray = buffer.toByteArray();
//                        buffer.reset();
//                        buffer.write(byteArray, 0, byteArray.length-1);
//                    }
//                } else if (data == CR || data == LF) {
//                    if (buffer.size() > 0) {
//                        printLine("");
//                        return buffer.toString();
//                    }
//                } else {
//                    buffer.write(data);
//                }
//            }
//        } catch (IOException e) {
//            throw new TerminalException(e);
//        }
    	return null;
	}

    @Override
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * @param movePrompt {@link #movePrompt}
     */
    public void setMovePrompt(boolean movePrompt) {
        this.movePrompt = movePrompt;
    }

    @Override
    public void close() throws TerminalException {
        try {
            socket.close();
        } catch (ClosedChannelException ignored) {
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    
    @Override
    protected boolean read() {
        try {
        	int numBytes = socket.read(buffer);
        	int numChars = 0;
        	buffer.flip();
        	
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                
                if (b == IAC) {
                	
                	int iacStart = buffer.position()-1;
                    try {
                    	subnegotiate(buffer);
                    } catch (BufferUnderflowException e) {
                    	buffer.position(iacStart);
                    	buffer.compact();
                    	return true;
                    }
                    
                } else {
//                	decodeKeyStroke(buffer.slice());
//                    for(CharacterPattern pattern : patterns) {
//                        Matching res = pattern.match(characterSequence);
//                        if (res != null) {
//                            if (res.partialMatch) { partialMatch = true; }
//                            if (res.fullMatch != null) { bestMatch = res.fullMatch; }
//                        }
//                    }
                	if (b == ESC) {
	                	int escapeStart = buffer.position()-1;
	                    try {
	                    	processEscape();
	                    } catch (BufferUnderflowException e) {
	                    	buffer.position(escapeStart);
	                    	buffer.compact();
	                    	return true;
	                    }
	                    log.info("Received: {} ({}) from {}", b, (char)b, this);
	                } else if (b == CR || b == LF) {
	
	                	if (data.length() > 0) {
	                    	if (echo) {
	                			socket.write(ByteBuffer.wrap(new byte[]{b}));
	                    	}
		                    for (Listener listener : listeners) {
		                        try {
		                            listener.onLine(this, data.toString());
		                        } catch (Exception e) {
		                            log.error("Error on terminal: " + this + ", data: " + data, e);
		                        }
		                    }
		                    data.setLength(0);
		                    cursor = 0;
	                	}
	                    
	                } else {
	                	
	                	if (b == BS || b == DELETE || b == EC) {
	                        if(data.length() > 0) {
	                        	data.deleteCharAt(--cursor);
	                        }
	                        numChars--;
	                	} else {
	                		data.insert(cursor++, (char)b);
	                		numChars++;
	                	}
	                	
	            		if (echo) {
	            			ByteBuffer tail = ByteBuffer.allocate((data.length()-cursor)*4 + 1);
	            			tail.put(b);
	            			for (int i = cursor; i < data.length(); i++) {
	            				tail.put((byte) data.charAt(i));
	            			}
	            			for (int i = cursor; i < data.length(); i++) {
	            				tail.put(ESC);
	            				tail.put(LEFT_BRACKET);
	            				tail.put(LEFT);
	            			}
	            			tail.flip();
	            			socket.write(tail);
	            		}
	            		
	                    for (Listener listener : listeners) {
	                        try {
	                            listener.onCharacter(this, (char) b);
	                        } catch (Exception e) {
	                            log.error("Error on terminal: " + this + ", data: " + data, e);
	                        }
	                    }
	                }
                }
            }
            
            if (numChars != 0) {
                for (Listener listener : listeners) {
                    try {
                        listener.onInput(this, numChars, data.toString());
                    } catch (Exception e) {
                        log.error("Error on terminal: " + this + ", data: " + data, e);
                    }
                }
            }
            
            buffer.clear();
            return numBytes != -1;
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }

	/**
     * @return the {@link Protocol} used by this terminal
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.TELNET;
    }
    
    /**
     * @return the underlying {@link SocketChannel} used by this terminal
     */
    @Override
    protected SocketChannel getSocket() {
        return socket;
    }
    
    /**
     * Handles telnet negotiation following receipt of an IAC
     * @throws EOFException if the connection is closed 
     * @throws IOException if an I/O error occurs
     */
    private void subnegotiate(ByteBuffer buffer) throws EOFException, IOException {
        byte b = buffer.get();
        if (b == WILL) {
            b = buffer.get();
            socket.write(ByteBuffer.wrap(new byte[]{IAC, DO, b}));
            log.info("Received WILL: {}", b);
        } else if (b == WONT) {
            b = buffer.get();
            socket.write(ByteBuffer.wrap(new byte[]{IAC, DONT, b}));
            log.info("Received WONT: {}", b);
        } else if (b == DO) {
            b = buffer.get();
            if (b == ECHO) {
            	echo = true;
                socket.write(ByteBuffer.wrap(new byte[]{IAC, WILL, ECHO}));
            } else {
                socket.write(ByteBuffer.wrap(new byte[]{IAC, WILL, b}));
            }
            log.info("Received do: {}", b);
        } else if (b == DONT) {
            b = buffer.get();
            socket.write(ByteBuffer.wrap(new byte[]{IAC, WONT, b}));
            log.info("Received DONT: {}", b);
        } else if (b == SB) { // Sub-negotiation
            for (;;) {
                // Read until end of negotiation
                b = buffer.get();
                if (b == NAWS) {
                    width = (short)(((0xFF & buffer.get()) <<8) | (0xFF  & buffer.get())) ;
                    height = (short)(((0xFF & buffer.get()) <<8) | (0xFF  & buffer.get()));
                    log.info("NAWS received: {}, {}", width, height);
                    
                    b = buffer.get();
                    if (b != IAC) {
                    	log.warn("Invalid NAWS received, IAC expected after dimensions, got: {}", b);
                    }
                } else if (b == SE) {
                    break;
                } else {
                    log.info("Received unknown subnegotiation: {} ", b);
                }
            }
        }
    }

    private void processEscape() throws IOException {
		byte b = buffer.get();
		if (b == LEFT_BRACKET) {
			b = buffer.get();
			if (b == LEFT) {
				if (cursor > 0) {
					cursor--;
	            	if (echo) {
	            		socket.write(ByteBuffer.wrap(new byte[]{ESC, LEFT_BRACKET, LEFT}));
	            	}
				}
			} else if (b == RIGHT) {
				if (cursor < data.length()) {
					cursor++;
	            	if (echo) {
	            		socket.write(ByteBuffer.wrap(new byte[]{ESC, LEFT_BRACKET, RIGHT}));
	            	}
				}
			} else {
				log.warn("Received unknown escape sequence: {} {}", LEFT_BRACKET, b);
			}
		} else {
			log.warn("Recived unknown escape code: {}", b);
		}
	}

    /**
     * Displays the prompt
     * @throws IOException if an I/O error occurs
     */
    private void prompt() throws IOException {
        String string = prompt + BasicColor.DEFAULT.toString();
        socket.write(ByteBuffer.wrap(string.getBytes()));
        promptDisplayed = true;
    }

    /**
     * Clears the last line if it displays the prompt
     * @throws IOException if an I/O error occurs
     */
    private void clearPrompt() throws TerminalException {
        if (movePrompt && promptDisplayed) {
//            socket.write(ByteBuffer.wrap(new byte[]{BS, BS, BS, BS}));
            promptDisplayed = false;
            log.debug("prompt cleared");
        }
    }

}
