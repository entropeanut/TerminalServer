import terminal.Protocol;
import terminal.Terminal;
import terminal.TerminalServer;
import terminal.telnet.TelnetTerminal;


/** TODO */
public class HudTest {

    @SuppressWarnings("unused")
	public static void main(String[] args) {
        final TerminalServer server = new TerminalServer();
        server.openPort(TelnetTerminal.DEFAULT_PORT, Protocol.TELNET);
    
        final Terminal terminal = server.accept();
    }
}
