package client_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	Socket socket;
	BufferedReader read;
	BufferedWriter output;
	DataInputStream inStream;
	DataOutputStream outStream;
	String clientMessage = "", serverMessage = "";
	BufferedReader br;
	
	public void startClient() throws UnknownHostException, IOException {
		System.out.println("startClient Methode wurde aufgerufen");
		try {
			Socket socket = new Socket("127.0.0.1", 8888);
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(inStream));

		} catch (Exception e) {
			System.out.println("Exception" + e);
		}
	}

	public boolean registrier(String name, String passwort)  throws IOException{
		clientMessage = "r" + " " + name + " " + passwort;

		try {
			outStream.writeUTF(clientMessage);
			outStream.flush();
			serverMessage = inStream.readUTF();
			System.out.println("antwort: " + serverMessage);

			if (serverMessage.equals("true")) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean login(String name, String passwort) throws IOException {

		clientMessage = "e" + " " + name + " " + passwort;
		try {
			outStream.writeUTF(clientMessage);
			outStream.flush();
			serverMessage = inStream.readUTF();
			System.out.println("antwort: " + serverMessage);

			if (serverMessage.equals("true")) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean logout(String name) {
		clientMessage = "lo" + " " + name;
		
		try {
			outStream.writeUTF(clientMessage);
			outStream.flush();
			serverMessage = inStream.readUTF();
			System.out.println("antwort: " + serverMessage);
			
			if (serverMessage.equals("ausgeloggt")) {
				System.out.println(serverMessage);
				return true;
			}else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}

	public void close() {

		try {
			outStream.close();
			outStream.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

}
