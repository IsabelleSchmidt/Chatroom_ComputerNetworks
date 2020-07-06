package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private final int SERVERPORT = 8888;
	
	private ServerData serverData;
	private ServerTCPThread clientThread;		// For register, login, logout, requests
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
			Socket clientSocket;
			
			@Override
			public void run() {
				
				// Pro Client wird ein TCP Thread gestartet
				while (serverOn) {
					try {
						clientSocket = serverSocket.accept();
						System.out.println("Client verbindet sich.....");
						
						clientThread = new ServerTCPThread(clientSocket, serverData);
						clientThread.start();
						
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

}
