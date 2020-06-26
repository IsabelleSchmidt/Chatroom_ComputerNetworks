package client_server;

import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
	
	public static void main(String[]args) throws Exception{
		Server s = new Server(8888);
		s.start();
	}

}
