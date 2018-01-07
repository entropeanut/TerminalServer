package terminal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A simple terminal server library for programs that want to communicate
 * with remote clients via a text-based interface. Currently offers very basic
 * support for the telnet protocol. */
public class TerminalServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TerminalServer.class);

    /** the multiplexer used to block on multiple listeners */
    private final Selector selector;
	
	/** the current clients */
	private final Set<Terminal> terminals = new HashSet<>();
	
	/** the observers that will be notified about client activity */
	private final Set<Listener> listeners = new HashSet<>();
	
	/**
	 * Constructs a new instance which will listen on the default
	 * @throws IOException if a network I/O exception occurs
	 */
	public TerminalServer() throws TerminalException {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new TerminalException(e);
        }
	}

    /**
     * Opens a port for new connections using the given protocol
     * @param port the port on which the server listens for connections
     * @param protocol the protocol to use for connections that are initiated 
     * from this port
     * @throws IOException if a network I/O exception occurs
     */
    public void openPort(int port, Protocol protocol) {
        try {
            ServerSocketChannel listener = ServerSocketChannel.open();
            listener.bind(new InetSocketAddress(port));
            listener.configureBlocking(false);
            listener.register(selector, SelectionKey.OP_ACCEPT, protocol);
            log.info("Opened port {} for {} connections", port, protocol);
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }
	
    /**
     * Closes the specified port to any further incoming connections.
     * Does not disconnect existing connections accepted through this port.
     * @param port the port to close
     * @throws IOException if a network I/O exception occurs
     */
    public boolean closePort(int port) {
        try {
            for (SelectionKey key : selector.keys()) {
                ServerSocketChannel listener = (ServerSocketChannel) key.channel();
                if (listener.socket().getLocalPort() == port) {
                    listener.keyFor(selector).cancel();
                    listener.close();
                    log.info("Closed port {} for {} connections", port, key.attachment());
                    return true;
                }
            }
            log.warn("closePort called for unopened port {}", port);
            return false;
        } catch (IOException e) {
            throw new TerminalException(e);
        }
    }
    
    /**
     * Continually waits for input, notifying listeners as events occur. 
     */
    @Override
    public void run() {
    	while (selector.isOpen()) {
	        try {
	            selector.select();
	            for (SelectionKey key : selector.selectedKeys()) {
	                if (key.isAcceptable()) {
	                    Terminal terminal = onAccept(key, false);
	                    for (Listener listener : listeners) {
	                        try {
	                            listener.onConnect(terminal);
	                        } catch (Exception e) {
	                            log.error("Error for terminal: " + terminal, e);
	                        }
	                    }
	                }
	                if (key.isReadable()) {
	                    Terminal terminal = (Terminal) key.attachment();
	                    try {
		                    if (!terminal.read()) {
	                    		disconnect(terminal);
	    	                    for (Listener listener : listeners) {
	    	                        try {
	    	                            listener.onDisconnect(terminal);
	    	                        } catch (Exception e) {
	    	                            log.error("Error for terminal: "  + terminal, e);
	    	                        }
	    	                    }
	                    	}
	                    } catch (Exception e) {
                            log.error("Error for terminal: "  + terminal, e);
                    		disconnect(terminal);
    	                    for (Listener listener : listeners) {
    	                        try {
    	                            listener.onDisconnect(terminal);
    	                        } catch (Exception e2) {
    	                            log.error("Error for terminal: "  + terminal, e2);
    	                        }
    	                    }
	                    }
	                }
	            }
	            selector.selectedKeys().clear();
	        } catch (IOException e) {
	            throw new TerminalException(e);
	        }
    	}
    }

    /**
     * Registers a listener for terminal events
     */
    public void listen(Listener listener) {
        if (listener == null)
            throw new NullPointerException("Listener cannot be null");
        listeners.add(listener);
    }
    
	/**
     * Returns a Terminal to a new remote client, blocking until a connection is
     * made
	 * @return a new Terminal to the remote client
	 * @throws IOException if a network I/O exception occurs
	 */
    public Terminal accept() {
        try {
            for (;;) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        return onAccept(key, true);
                    }
                }
            }
        } catch (IOException e) {
            throw new TerminalException(e);
        }
	}

    private Terminal onAccept(SelectionKey key, boolean isBlocking) throws IOException {
        ServerSocketChannel listener = (ServerSocketChannel) key.channel();
        SocketChannel socket = listener.accept();
        socket.configureBlocking(isBlocking);
        
        Protocol protocol = (Protocol) key.attachment();
        SocketAddress addr = socket.getRemoteAddress();
        int port = listener.socket().getLocalPort();
        log.info("Accepted new {} connection from {} on port {}", protocol, addr, port);
        
        Terminal terminal = protocol.newInstance(socket);
        socket.register(selector, SelectionKey.OP_READ, terminal);
        return terminal;
    }

	/**
	 * Disconnects the given client
	 * @param terminal the client to disconnect
     * @throws IOException if a network I/O exception occurs
	 */
	public void disconnect(Terminal terminal) {
	    try {
            String type = terminal.getClass().getSimpleName();
            SocketAddress addr = terminal.getSocket().getRemoteAddress();

            terminals.remove(terminal);
            terminal.close();
            
			log.info("Disconnected {} connection from {}", type, addr);
        } catch (IOException e) {
            throw new TerminalException(e);
        }
	}

    public interface Listener {
        
        /** Fires when a new connection is accepted */
        void onConnect(Terminal terminal);
        
        /** Fired when a connection is terminated */
        void onDisconnect(Terminal terminal);
    }
}
