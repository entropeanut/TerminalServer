package terminal.http;

import java.nio.channels.SocketChannel;

import terminal.Protocol;
import terminal.Terminal;
import terminal.TerminalException;
import terminal.Terminal.Listener;

public class HttpTerminal extends Terminal {

	private SocketChannel socket;

	public HttpTerminal(SocketChannel socket) {
		this.socket = socket;
	}

	@Override
	public void print(String text) throws TerminalException {
		// TODO Auto-generated method stub

	}

	@Override
	public void printLine(String text) throws TerminalException {
		// TODO Auto-generated method stub

	}

	@Override
	public String readLine() throws TerminalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws TerminalException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrompt(String prompt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(Listener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Protocol getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SocketChannel getSocket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean read() {
		// TODO Auto-generated method stub
		return false;
	}

}
