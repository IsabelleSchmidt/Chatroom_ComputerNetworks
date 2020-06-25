package client_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPServer {

	private Map<String, String> benutzer;			//Map für alle registrierten User mit Passwort
	private List<String> activeUser;				//Liste für alle eingeloggten User

	boolean korrekt = false;
	boolean vergeben = true;

	public TCPServer() {
		this.benutzer = new HashMap<>();
		this.activeUser = new ArrayList<>();
	}

	public void start() {
		try {
			ServerSocket server = new ServerSocket(8888);
			int counter = 0;
			System.out.println("Server started.....");
			
			while(true) {
				counter++;
				Socket serverClient = server.accept();
				System.out.println(" >> " + "Client No:" + counter + " started!");
				ServerClientThreadTCP sct = new ServerClientThreadTCP(serverClient, counter);
				sct.start();
			}
			}catch(Exception e) {
				System.out.println(e);
			}
	}
	
	public static void main(String args[]){
        TCPServer server = new TCPServer();
        server.start();
    }

}
