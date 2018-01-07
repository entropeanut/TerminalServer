package terminal;


/**
 * Callback interface provided by <code>Terminal</code>
 */
public interface TerminalClient {

    /**
     * Called when a terminal is opened
     * @param terminal the terminal that was opened
     */
    public void onOpen(Terminal terminal);
    
    /**
     * Called when a terminal receives data
     * @param terminal the terminal that received data
     * @param data the data that was received
     */
//    public void onRead(TelnetTerminal terminal, int data);
    
    /**
     * Called when a terminal receives a carriage return and/or line feed
     * @param terminal the terminal that received the CR/LF
     */
//    public void onEnter(TelnetTerminal terminal);
    
    /**
     * Called when a terminal is closed
     * @param terminal the terminal that was closed
     */
    public void onClose(Terminal terminal);
    
    /**
     * Called when a terminal needs to display the prompt prior to requesting
     * user input
     * @param terminal the terminal that is requesting the prompt
     * @return the prompt to display
     */
//    public String getPrompt(TelnetTerminal terminal);
}
