package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import message.Command;
import message.Message;
import message.Status;

public class ClientUDPThread extends Thread {
	
	private DatagramSocket udpSocket;
	private boolean socketOn;
	private Thread chatListener;
	private ClientChunkThread chunkThread;
	
	public ClientUDPThread() {
		this.socketOn = true;
		initChatListener();
	}
	
	@Override
	public void run() {
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		socketOn = true;
		
		chatListener.start();
	}
	
	public void startChunkThread(String otherClient, Message message) {
		chunkThread = new ClientChunkThread(udpSocket, otherClient, message.chunk());
		chunkThread.start();
	}
	
	private void initChatListener() {
		chatListener = new Thread() {
			@Override
			public void run() {
				// Empfange Nachrichten solange Socket geoeffnet ist
				while (socketOn) {
					DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
					Message serverMessage;
					
					//Versuche Nachrichten zu empfangen
					try {
						udpSocket.receive(reply);
						serverMessage = new Message(new String(reply.getData()).trim());
					    System.out.println("CLIENT: Message from " + reply.getPort() + ": " + new String(reply.getData()) + "...");
					    
					    Command serverCommand = serverMessage.getCommand();
					    
					    switch (serverCommand) {
						case CHUNK_SAVED:
							System.out.println(serverMessage.getRaw() + "...");
							int chunkNr = Integer.parseInt(serverMessage.getAttributes().get("chunkNr"));
							chunkThread.acks[chunkNr] = Status.ACKED;
							System.out.println("CLIENT: Nummer " + chunkNr + " is acked");
							break;
							
						case NEW_TEXT_MESSAGE:
							String text = serverMessage.getAttributes().get("text");
							System.out.println(text);
							break;
							
						default:
							System.out.println("Client UDP Switch funktioniert nicht");
						}
						
					} catch (IOException e) {
						if (!socketOn) {
							System.out.println("CLIENT: client socket is closed.");
						} else {
							e.printStackTrace();
						}
					}
				}
			}
		};
	}
	
	public void closeUDPSocket() {
		socketOn = false;
		udpSocket.close();
	}

}
