package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private final int SERVERPORT = 8888;
	
	private ServerData serverData;
	private ServerSocket serverSocket;
	private boolean serverOn = true;

	public Server() {
		serverData = new ServerData();
		try {
			serverSocket = new ServerSocket(this.SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		System.out.println("Server ist gestartet.");
		
		new Thread() {
	
			@Override
			public void run() {
	
				while (serverOn) {
					try {
						Socket clientSocket = serverSocket.accept();
						System.out.println("Client verbindet sich.....");
						
						// Pro Client wird ein TCP Thread gestartet
						final ServerTCPThread thread = new ServerTCPThread(clientSocket, serverData);
						thread.start();
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}	
		}.start();
	}
	
	public void stop() {
		serverOn = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerData getServerData() {
		return serverData;
	}
	
	public static void main (String[] args) {
		Server server = new Server();
		server.start();
	}

}