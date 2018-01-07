package terminal;

import java.nio.channels.SocketChannel;

import terminal.http.HttpTerminal;
import terminal.telnet.TelnetTerminal;

/** The supported terminal protocols */
public enum Protocol {
    TELNET() {
        @Override
        public Terminal newInstance(SocketChannel socket) {
            return new TelnetTerminal(socket);
        }
    },
    HTTP() {
        @Override
        public Terminal newInstance(SocketChannel socket) {
            return new HttpTerminal(socket);
        }
    };
    
    abstract public Terminal newInstance(SocketChannel socket);
}
