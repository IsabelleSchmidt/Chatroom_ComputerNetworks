package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import message.Chunk;
import message.Command;
import message.Message;
import message.MessageGenerator;
import message.Status;
import server.EndpointInfo;

public class ClientUDPThread extends Thread {
	
	private DatagramSocket udpSocket;
	private boolean socketOn;
	private Thread chatListener;
	private ClientChunkThread chunkThread;
	
	private String thisClientName;
	protected Map<EndpointInfo, ChatData> chatData;
	protected Map<String, EndpointInfo> connectionData;
	
	public ClientUDPThread(String thisClient) {
		this.chatData = new HashMap<>();
		this.connectionData = new HashMap<>();
		this.thisClientName = thisClient;
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
	
	public void addChatData(EndpointInfo info, ChatData data) {
		chatData.put(info, data);
	}
	
	public void addConnectionData(String name, EndpointInfo info) {
		connectionData.put(name, info);
	}

	public void startChunkThread(Message message, EndpointInfo otherClientInfo) {
		chunkThread = new ClientChunkThread(udpSocket, otherClientInfo, message.chunk());
		chunkThread.start();
	}
	
	private void initChatListener() {
		chatListener = new Thread() {
			@Override
			public void run() {
				// Empfange Nachrichten solange Socket geoeffnet ist
				while (socketOn) {
					DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
					
					//Versuche Nachrichten zu empfangen
					try {
						udpSocket.receive(reply);
						String messageStr = new String(reply.getData());
					    System.out.println("CLIENT: Message from " + reply.getPort() + ": " + messageStr + "...");
					    
					    
					   
					    if (messageStr.startsWith("CHUNK")) {
					    	Chunk clientChunk = Chunk.parse(messageStr);
							int chunkNr = clientChunk.getChunkNr();
							chunkThread.acks[chunkNr] = Status.ACKED;
							System.out.println("CLIENT: Nummer " + chunkNr + " is acked.");
							saveMessage(clientChunk, reply.getAddress(), reply.getPort());
							
							Message message = MessageGenerator.chunkReceived(thisClientName, chunkNr);
							sendMessage(message, reply.getAddress(), reply.getPort());
					    	
					    } else {
					    	Message message = new Message(messageStr);
					    	Command serverCommand = message.getCommand();
					    	
					    	switch (serverCommand) {
							case CHUNK_RECEIVED:
								System.out.println(message.getRaw() + "...");
								int chunkNr = Integer.parseInt(message.getAttributes().get("chunkNr"));
								chunkThread.acks[chunkNr] = Status.ACKED;
								System.out.println("CLIENT: Nummer " + chunkNr + " is acked");
								break;
							default:
								System.out.println("Client UDP Switch funktioniert nicht");
					    	}
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
	
	private void saveMessage(Chunk clientChunk, InetAddress address, int port) {
		EndpointInfo info = new EndpointInfo(address, port);
		chatData.get(info).addChunk(clientChunk);
	}
	
	private void sendMessage(Message message, InetAddress address, int port) {
		String messageStr = message.getRaw() + "\n";
		DatagramPacket packet = new DatagramPacket(messageStr.getBytes(), messageStr.length(), address, port);
		try {
			udpSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeUDPSocket() {
		socketOn = false;
		udpSocket.close();
	}

}
