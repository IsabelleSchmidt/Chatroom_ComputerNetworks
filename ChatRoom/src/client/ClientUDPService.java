package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import application.Config;
import message.Chunk;
import message.Command;
import message.Message;
import message.MessageGenerator;
import message.Status;
import server.EndpointInfo;


public class ClientUDPService {
	
	private DatagramSocket udpSocket;
	private boolean socketOn;
	private Thread chatListener;
	private ClientChunkThread chunkThread;
	private int udpPort;
	
	private String thisClientName;
	protected Map<EndpointInfo, ChatData> chatData;
	protected Map<String, EndpointInfo> connectionData;
	
	public ClientUDPService(String thisClient, int udpPort) {
		this.chatData = new HashMap<>();
		this.connectionData = new HashMap<>();
		this.thisClientName = thisClient;
		this.socketOn = false;
		this.udpPort = udpPort;
		initChatListener();
	}
	
	public void start() {
		try {
			udpSocket = new DatagramSocket(udpPort);
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
		int messageNr = chatData.get(otherClientInfo).getMessages().size();
		chunkThread = new ClientChunkThread(udpSocket, otherClientInfo, message.chunk(messageNr));
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

					   
					    if (messageStr.startsWith("CHUNK|")) {
					    	String messageCut = messageStr.substring(0, Config.RECEIVE_BUFFER_LENGTH - 1);
					    	Chunk clientChunk = Chunk.parse(messageCut);
							int chunkNr = clientChunk.getChunkNr();
							saveMessage(clientChunk, reply.getAddress(), reply.getPort());
							
							Message message = MessageGenerator.chunkReceived(chunkNr);
							sendMessage(message, reply.getAddress(), reply.getPort());
					    	
					    } else {
					    	Message message = new Message(messageStr.trim());
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
	
	public void stop() {
		socketOn = false;
		chatListener.interrupt();
		udpSocket.close();
		System.out.println("Close UDP Socket, interrupt listener");
	}

	public boolean isSocketOn() {
		return socketOn;
	}
	
	

}
