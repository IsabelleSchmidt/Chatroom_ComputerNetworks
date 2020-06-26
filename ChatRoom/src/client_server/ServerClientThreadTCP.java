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
	private Map<Integer, Boolean> ausloggen = new HashMap<>();
	
	boolean korrekt;
	boolean vergeben;
	TCPServer server;

	ServerClientThreadTCP() {
		
	}
	
	public synchronized void startNewThread(TCPServer server, Socket inSocket, int counter) {
		this.server = server;
		serverClient = inSocket;
		clientNo = counter;
		this.start();
		
	}

	public void run() {
		try {
			DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
			String clientMessage = "", serverMessage = "";

			while (!clientMessage.equals("bye")) {
				clientMessage = inStream.readUTF();
				System.out.println("From Client-" + clientNo + ": Message is :" + clientMessage);

				if (clientMessage.startsWith("r")) {

					String m = clientMessage.replace("r", "");
					String[] s = m.split(" ");
					if (testUsername(s[0]) == false) {
						registerUser(s[0], s[1]);
						
						benutzer.put(s[0], s[1]);
						
						serverMessage = "true";
						registrieren.put(clientNo, false);
					} else if (testUsername(s[0]) == false) {
						serverMessage = "false";
					}
				}

				else if (clientMessage.startsWith("e")) {
					String m = clientMessage.replace("e", "");
					String[] s = m.split(" ");
					if (login(s[0], s[1]) == false) {
						serverMessage = "false";
					} else {
						serverMessage = "true";
						einloggen.put(clientNo, false);
					}

				}
				
				else if (clientMessage.startsWith("lo")) {
					String m = clientMessage.replace("lo", "");
					String[] s = m.split(" ");
					loggoutUser(s[0]);
					serverMessage = "ausgeloggt";
					ausloggen.put(clientNo, false);
					
				}
				
				else if (ausloggen.get(clientNo) == true) {
					serverMessage = "Du wirst jetzt ausgeloggt";
					ausloggen.put(clientNo, false);
				}

				else if (registrieren.get(clientNo) == true) {
					String[] s = clientMessage.split(" ");
					if (testUsername(s[0]) == false) {
						registerUser(s[0], s[1]);
						serverMessage = "From Server to Client-" + clientNo + "  Willkommen im Chat!";
						
						benutzer.put(s[0], s[1]);
						registrieren.put(clientNo, false);
					} else if (testUsername(s[0]) == true) {
						serverMessage = "From Server to Client-" + clientNo
								+ "  Dieser Benutzername ist leider schon vergeben";
					}

				}

				else if (einloggen.get(clientNo) == true) {
					String[] s = clientMessage.split(" ");
					if (login(s[0], s[1]) == false) {
						serverMessage = "From Server to Client-" + clientNo
								+ "  Da stimmt etwas nicht, bitte versuche es erneut!";
					} else {
						serverMessage = "From Server to Client-" + clientNo + "  Herzlich Willkommen im Chat!";
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
	
	public void loggoutUser(String name) {
		
		for (String n : activeUser) {
			if(n.equals(name)) {
				activeUser.remove(n);
			}
		}
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
