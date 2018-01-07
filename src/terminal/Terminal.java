package terminal;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Common interface implemented by all terminal types
 */
public abstract class Terminal {

    /** The current client width, in characters, or null if unknown */
    protected Short width;
    
    /** The current client height, in characters, or null if unknown */
    protected Short height;

    /**
     * Prints text on the client's screen
     * @param text the message to print
     */
    abstract public void print(String text) throws TerminalException;

    /**
     * Prints a line of text on the client's screen with the default color
     * @param text the message to print
     */
    abstract public void printLine(String text) throws TerminalException;

    /**
     * Returns a line of input from the remote client
     * @return the next line of client input
     */
    abstract public String readLine() throws TerminalException;

    /**
     * Closes the terminal
     */
    abstract public void close() throws TerminalException;

    /**
     * @param prompt the prompt that is displayed when ready to accept input 
     */
    abstract public void setPrompt(String prompt);

    /**
     * Registers a listener for terminal events
     */
    abstract public void addListener(Listener listener);
    
    /**
     * @return the {@link Protocol} used by this terminal
     */
    abstract public Protocol getProtocol();

    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "=["
                + getSocket().getRemoteAddress() + "]";
        } catch (IOException e) {
            return getClass().getSimpleName() + "(disconnected)";
        }
    }

    /**
     * @return the underlying {@link SocketChannel} used by this terminal
     */
    abstract protected SocketChannel getSocket();

    /**
     * Reads available input from the client connection
     * @return true if successful, false if the end of stream was reached
     */
    abstract protected boolean read();
    
    /**
     * Interface for non-blocking client implementations that wish to be notified of terminal-related events.
     * These methods need to return in a timely manner. Long running operations should be performed asynchronously
     * on a separate thread.
     */
    public interface Listener {
        
        /** Fires as each character is received */
        void onCharacter(Terminal terminal, char data);
        
        /** Fires when new data is received
         * @param numChars the number of chars received. May be negative if more characters were deleted than added.
         * @param input the input received since the last EOL
         */
        void onInput(Terminal terminal, int numChars, String current);
        
        /** Fires when an EOL is received */
        void onLine(Terminal terminal, String line);
    }

	public Short getWidth() {
		return width;
	}

	public Short getHeight() {
		return height;
	}
}