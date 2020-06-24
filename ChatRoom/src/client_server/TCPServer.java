package client_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPServer {

	private final int PORT = 1234;
	private Map<String, String> benutzer;
	private List<String> activeUser;

	boolean korrekt = false;
	boolean vergeben = true;

	public TCPServer() {
		this.benutzer = new HashMap<>();
		this.activeUser = new ArrayList<>();
	}

	public void start() {
		try {
			final ServerSocket serverSocket = new ServerSocket(PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Server: Client hat sich verbunden.");

				final Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						final BufferedReader reader;
						final BufferedWriter writer;

						try {

							reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

							System.out.println("SCHALOOOMSKI. EINLOGGEN ODER REGISTRIEREN?");
							String antwort = reader.readLine();
							System.out.println(antwort);

							if (antwort.equals("einloggen")) {
								while (korrekt == false) {
									System.out.println("WIE HEISCHT DU?");
									String name = reader.readLine();
									System.out.println("WIE ISCH DEIN PASSCHWORT?");
									String passwort = reader.readLine();
									System.out.println("MA GUCKEN OB DES SCHTIMMT");
									login(name, passwort);
								}
							} else if (antwort.equals("registrieren")) {
								while (vergeben == true) {
									System.out.println("WIE WILLSCHT DU HEISCHEN?");
									String name = reader.readLine();
									System.out.println("UND DEIN PASSCHWORT? ABER GEHEIM HALTEN NH");
									String passwort = reader.readLine();

									if (testUsername(name) == false) {
										registerUser(name, passwort);
									} else {
										System.out.println("SORRY DISCH GIBBET SCHON.");
									}
								}

							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					}

				});
				thread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean login(String name, String passwort) {
		for (String n : benutzer.keySet()) {
			if (n.equals(name)) {
				if (passwort.equals(benutzer.get(n))) {
					System.out.println("DU BISCH EINGELOGGT, GEIL");
					return korrekt = true;
				} else {
					System.out.println("INKORREKT, NOCHMAL ARSCHLOCH");
					return korrekt = false;
				}

			}
		}
		System.out.println("SORRY DISCH KENNEN WA NICHT");
		return korrekt = false;
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
