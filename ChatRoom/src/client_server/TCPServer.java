package client_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class TCPServer {

	ServerClientThreadTCP sct = new ServerClientThreadTCP();
	ServerSocket server;

	int clientNo;
	private Map<String, String> benutzer = new HashMap<>();
	public ObservableList<String> activeUser = FXCollections.observableArrayList();

	private Map<Integer, Boolean> einloggen = new HashMap<>();
	private Map<Integer, Boolean> registrieren = new HashMap<>();
	private Map<Integer, Boolean> ausloggen = new HashMap<>();

	boolean korrekt;
	boolean vergeben;
	boolean notclosed = true;

	private final int SERVERPORT = 8888;

	public TCPServer() {
		try {
			server = new ServerSocket(this.SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public List<String> getActiveUsers() {
		return activeUser;
	}

	public void start() {
		
		while (true) {
			try {
				Socket socket = server.accept();
				System.out.println("Client verbindet sich.....");

				clientNo++;
				System.out.println(" >> " + "Client No:" + clientNo + " started!");

				final Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						DataInputStream inStream;
						DataOutputStream outStream;

						try {
							inStream = new DataInputStream(socket.getInputStream());
							outStream = new DataOutputStream(socket.getOutputStream());

							while(notclosed) {
								handleRequest(socket, inStream, outStream);
							}
		
							try {
								inStream.close();
								outStream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
					}

				});
				thread.start();
			} catch (IOException e) {

			}
		}
	}

	public void handleRequest(final Socket socket, final DataInputStream inStream, final DataOutputStream outStream) {
		String clientMessage = "", serverMessage = "";

		try {
			clientMessage = inStream.readUTF();

			System.out.println("From Client-" + clientNo + ": Message is :" + clientMessage);

			if (clientMessage.startsWith("r")) {

				String m = clientMessage.replace("r", "");
				String[] s = m.split(" ");
				
				if (testUsername(s[1]) == false) {
					registerUser(s[1], s[1]);
					activeUser.add(s[1]);
					System.out.println(activeUser);
					benutzer.put(s[1], s[1]);
					

					serverMessage = "true";
				} else if (testUsername(s[1]) == false) {
					serverMessage = "false";
				}
			}

			else if (clientMessage.startsWith("e")) {
				String m = clientMessage.replace("e", "");
				String[] s = m.split(" ");
				if (login(s[1], s[2]) == false) {
					
					serverMessage = "false";
				} else {
					activeUser.add(s[1]);
					serverMessage = "true";
					einloggen.put(clientNo, false);
				}

			}

			else if (clientMessage.startsWith("lo")) {
				String m = clientMessage.replace("lo", "");
				String[] s = m.split(" ");
				loggoutUser(s[1]);
				serverMessage = "ausgeloggt";
				ausloggen.put(clientNo, false);

			}

			else if (ausloggen.get(clientNo) == true) {
				serverMessage = "Du wirst jetzt ausgeloggt";
				ausloggen.put(clientNo, false);
			}

//			else if (registrieren.get(clientNo) == true) {
//				String[] s = clientMessage.split(" ");
//				System.out.println("NAME: " + s[0]);
//				if (testUsername(s[0]) == false) {
//					registerUser(s[0], s[1]);
//					serverMessage = "From Server to Client-" + clientNo + "  Willkommen im Chat!";
//
//					benutzer.put(s[0], s[1]);
//					registrieren.put(clientNo, false);
//				} else if (testUsername(s[0]) == true) {
//					serverMessage = "From Server to Client-" + clientNo
//							+ "  Dieser Benutzername ist leider schon vergeben";
//				}
//
//			}

//			else if (einloggen.get(clientNo) == true) {
//				String[] s = clientMessage.split(" ");
//				if (login(s[0], s[1]) == false) {
//					serverMessage = "From Server to Client-" + clientNo
//							+ "  Da stimmt etwas nicht, bitte versuche es erneut!";
//				} else {
//					serverMessage = "From Server to Client-" + clientNo + "  Herzlich Willkommen im Chat!";
//					einloggen.put(clientNo, false);
//				}
//
//			}
			outStream.writeUTF(serverMessage);
			outStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
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
			if (n.equals(name)) {
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
	
	public void stop() {
        if (notclosed == false) {
            notclosed = true;
            try {
            	System.out.println("Schliesse Server Socket...");
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public ObservableList<String> getActiveUser() {
		return activeUser;
	}


}
