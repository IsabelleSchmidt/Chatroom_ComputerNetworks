package client_server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
	private List<ServerClientThreadTCP> activeUser = new ArrayList<>();
	private int serverPort;

	public Server(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public List<ServerClientThreadTCP> getActiveUsers(){
		return activeUser;
	}
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(serverPort);
			int counter = 0;
			System.out.println("Server started.....");
			
			while(true) {
				counter++;
				Socket serverClient = server.accept();
				System.out.println(" >> " + "Client No:" + counter + " started!");
				ServerClientThreadTCP sct = new ServerClientThreadTCP(this, serverClient, counter);
				activeUser.add(sct);
				sct.start();
			}
			}catch(Exception e) {
				System.out.println(e);
			}
	}
}
