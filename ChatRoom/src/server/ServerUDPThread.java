package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import message.Chunk;
import message.Message;
import message.MessageGenerator;

public class ServerUDPThread extends Thread {
	
	private boolean socketOn;
	DatagramSocket socket;
	final int PORT;
	
	List<Chunk> chunks;
	
	public ServerUDPThread(int port) {
		this.chunks = new ArrayList<>();
		this.socketOn = true;
		this.PORT = port;
	}
	
	@Override
	public void start() {
		new Thread() {
			@Override
			public void run() {
				Chunk clientChunk;
		
				try {
					socket = new DatagramSocket(PORT);
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
				socketOn = true;
		
				while (socketOn) {
					DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
		
					// Es passiert nichts, bis ein Paket empfangen wird
					try {
						socket.receive(request);
						System.out.println("Server: Received from " + request.getAddress().getHostAddress() + ": " + new String(request.getData()));
						
						clientChunk = Chunk.parse(new String(request.getData()));
						saveMessage(clientChunk);
			
						// Hier wird dann entschieden, ob ein Paketverlust simuliert wird, oder
						// tatsaechlich eine positive Antwort zu senden
		//				if (random.nextDouble() < LOSS_RATE) {
		//					System.out.println(this.name + ": " + "Simuliere Verzoegerung fuer PING " + clientMessage.getNr());
		//					packets[clientMessage.getNr()] = Status.LOST;
		//					continue;
		//				} else {
							Message message = MessageGenerator.chunkSaved(clientChunk.getChunkNr());
							sendMessage(message, request.getAddress(), request.getPort());
		//				}
			
						// Hier wird bei positivem Empfang die Verzoegerung des Programms simuliert
		//				Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
						
					} catch (IOException e) {
						if (!socketOn) {
							System.out.println("ServerChatThread: server socket is closed.");
						} else {
							e.printStackTrace();
						}
					}
				}
				
			}
		}.start();
	}
	
	private void saveMessage(Chunk clientChunk) {
		chunks.add(clientChunk);
		Collections.sort(chunks);
	}
	
	private void sendMessage(Message message, InetAddress address, int port) {
//		System.out.println(message.getRaw() + ", " + message.getRaw().length());
		String messageStr = message.getRaw() + "\n";
		DatagramPacket packet = new DatagramPacket(messageStr.getBytes(), messageStr.length(), address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void closeUDPSocket() {
		socketOn = false;
		socket.close();
		System.out.println("ServerChatThread: server socket is closed.");
	}

}
