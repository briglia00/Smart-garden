package gardenservice;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class DashboardService extends WebSocketServer {
    
	public DashboardService(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
	    System.out.println("Dashboard started on address: " + this.getAddress());
	}

	public DashboardService(InetSocketAddress address) {
	    super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
	    conn.send("Welcome!"); //This method sends a message to the new client
	    broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
	    System.out.println("Connected to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	    System.out.println(conn + " has disconnected");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	    broadcast(message);
	    System.out.println(conn + ": " + message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
	    broadcast(message.array());
	    System.out.println(conn + ": " + message);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	    ex.printStackTrace();
	}

	@Override
	public void onStart() {
	    System.out.println("Dashboard Service Started!");
	    setConnectionLostTimeout(0);
	    setConnectionLostTimeout(100);
	}
	
	public void sendToDashboard(String message) {
		broadcast(message);
	}
}
