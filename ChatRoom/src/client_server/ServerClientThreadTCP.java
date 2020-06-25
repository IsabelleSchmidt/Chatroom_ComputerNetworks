package client_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerClientThreadTCP extends Thread {

	Socket serverClient;
	int clientNo;
	private Map<String, String> benutzer = new HashMap<>();
	private List<String> activeUser = new ArrayList<>();

	private Map<Integer, Boolean> einloggen = new HashMap<>();
	private Map<Integer, Boolean> registrieren = new HashMap<>();
	boolean korrekt;
	boolean vergeben;

	ServerClientThreadTCP(Socket inSocket,int counter){
	    serverClient = inSocket;
	    clientNo=counter;
	  }

	public void run() {
		try {
			DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
			String clientMessage = "", serverMessage = "";

			while (!clientMessage.equals("bye")) {
				clientMessage = inStream.readUTF();
				System.out.println("From Client-" + clientNo + ": Message is :" + clientMessage);

				if (clientMessage.equals("r")) {
					serverMessage = "From Server to Client-" + clientNo
							+ " Bitte geb ein Benutzername und Passwort ein um dich zu registrieren ";
					registrieren.put(clientNo, true);
				}

				else if (clientMessage.equals("e")) {
					serverMessage = "From Server to Client-" + clientNo
							+ " Bitte geb ein Benutzername und Passwort ein um dich anzumelden ";
					einloggen.put(clientNo, true);
				}

				else if (registrieren.get(clientNo) == true) {
					String[] s = clientMessage.split(" ");
					if (testUsername(s[0]) == false) {
						registerUser(s[0], s[1]);
						serverMessage = "From Server to Client-" + clientNo + "Willkommen im Chat!";
						registrieren.put(clientNo, false);
					} else {
						serverMessage = "From Server to Client-" + clientNo
								+ "Dieser Benutzername ist leider schon vergeben";
					}

				}

				else if (einloggen.get(clientNo) == true) {
					String[] s = clientMessage.split(" ");
					if (login(s[0], s[1]) == false) {
						serverMessage = "From Server to Client-" + clientNo
								+ "Da stimmt etwas nicht, bitte versuche es erneut!";
					} else {
						serverMessage = "From Server to Client-" + clientNo + "Herzlich Willkommen im Chat!";
						einloggen.put(clientNo, false);
					}

				}

				outStream.writeUTF(serverMessage);
				outStream.flush();
			}
			inStream.close();
			outStream.close();
			serverClient.close();
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			System.out.println("Client -" + clientNo + " exit!! ");
		}
	}

	public boolean login(String name, String passwort) {
		for (String n : benutzer.keySet()) {
			if (n.equals(name)) {
				if (passwort.equals(benutzer.get(n))) {
					return true;
				} else {
					return false;
				}

			}
		}
		return false;
	}

	public void registerUser(String name, String passwort) {
		benutzer.put(name, passwort);
	}

	public boolean testUsername(String name) {
		for (String n : benutzer.keySet()) {
			if (n.equals(name)) {
				return vergeben = true;
			}
		}

		return vergeben = false;
	}

}
