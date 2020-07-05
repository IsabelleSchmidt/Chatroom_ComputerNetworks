package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import message.ChatMessage;
import message.Command;
import message.Message;
import message.MessageGenerator;


public class Client {

	private Map<String, ChatData> chatMap;
	private InetAddress serverAddress;
	private int serverPort = 8888;
	private String name;
	
	//TCP
	Socket socket;
	BufferedReader read;
	BufferedWriter output;
	DataInputStream inStream;
	DataOutputStream outStream;
	BufferedReader br;
	
	// UDP
	ClientUDPThread udpThread; // Eins pro User?
	private InetAddress clientAddress;
	private int clientPort;
	
	public Client(String name, int port) {
		this.name = name;
		this.chatMap = new HashMap<>();
		
		// Init Server IP Address
		try {
			this.serverAddress = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Init Client IP Address
		try {
			this.clientAddress = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Init Client port
		this.clientPort = port;
	}
	
	public void startClient() throws UnknownHostException, IOException {
		System.out.println("startClient Methode wurde aufgerufen");
		
		// Socket, Output, Input, Reader
		try {
			Socket socket = new Socket(serverAddress.getHostAddress(), serverPort);
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(inStream));

		} catch (Exception e) {
			System.out.println("Exception" + e);
		}
	}
	
	public void closeTCPSocket() {
		try {
			outStream.close();
			outStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean registrier(String name, String passwort) throws IOException {
		Message clientMessage;
		Message serverMessage;
		
		clientMessage = MessageGenerator.register(name, passwort);
		System.out.println(this.name + ": send to Server - " + clientMessage.getRaw());
		
		try {
			outStream.writeUTF(clientMessage.serialize());
			outStream.flush();
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());

			if (serverMessage.getCommand() == Command.REGISTER_ACCEPTED) {
				return true;
			} else if (serverMessage.getCommand() == Command.REGISTER_DECLINED) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean login(String name, String passwort) throws IOException {
		Message clientMessage;
		Message serverMessage;

		clientMessage = MessageGenerator.login(name, passwort);
		try {
			outStream.writeUTF(clientMessage.serialize());
			outStream.flush();
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());

			if (serverMessage.getCommand() == Command.LOGIN_ACCEPTED) {
				return true;
			} else if (serverMessage.getCommand() == Command.LOGIN_DECLINED) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean logout(String name) {
		Message clientMessage;
		Message serverMessage;
		
		clientMessage = MessageGenerator.logout(name);
		
		try {
			outStream.writeUTF(clientMessage.serialize());
			outStream.flush();
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());
			
			if (serverMessage.getCommand() == Command.LOGEDOUT) {
				return true;
			}else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean sendChatRequest(String otherClientName) {
		Message clientMessage;
		Message serverMessage = null;
		
		clientMessage = MessageGenerator.sendRequest(otherClientName, clientAddress, clientPort);
		
		try {
			outStream.writeUTF(clientMessage.serialize());
			outStream.flush();
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Command serverCommand = serverMessage.getCommand();
		
		switch (serverCommand) {
		case REQUEST_ACCEPTED:
			System.out.println(this.name + ": Request accepted. Start new chat.");
			String clientName = serverMessage.getAttributes().get("requestRecipient"); //name
			newChat(clientName);
			return true;
		
		case REQUEST_DECLINED:
			System.out.println(this.name + ": Request declined.");
			return false;
		default:
			return false;
		}
	}
	
	public boolean acceptRequest(String requestSender) {
		Message clientMessage;
		Message serverMessage = null;
		clientMessage = MessageGenerator.acceptRequest(requestSender);
		System.out.println(this.name + ": sent to Server - " + clientMessage.getRaw());
		
		//TODO: Remove all from serverMessage, boolean method to void
		try {
			outStream.writeUTF(clientMessage.getRaw());
			outStream.flush();
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Command serverCommand = serverMessage.getCommand();
		
		switch (serverCommand) {
		case REQUEST_ACCEPTED:
			System.out.println(this.name + ": Request accepted. Start new chat.");
			String clientName = serverMessage.getAttributes().get("requestRecipient"); //name
			newChat(clientName);
			return true;
		
		case REQUEST_DECLINED:
			System.out.println(this.name + ": Request declined.");
			return false;
		default:
			return false;
		}
	}

	private void newChat(String otherClient) {
		// Chat Data vom anderen User erstellen
		ChatData chatData = new ChatData(otherClient);
		chatMap.put(otherClient, chatData);
		
		// Start UDP Thread
		// Bei jeder Chat Bestaetigung wird ein chatThread gestartet
//		chatThread = new ServerUDPThread(SERVERPORT, );
//		chatThread.start();
		udpThread = new ClientUDPThread();
		udpThread.start();
	}
	
	public void sendTextMessage(String otherClient, String text) {
//		newChat(otherClient);
		ChatMessage newMessage = new ChatMessage(this.name, text);
		chatMap.get(otherClient).addMessage(newMessage);
		
		Message message = MessageGenerator.sendTextMessage(text, otherClient, newMessage.getTime());
		udpThread.startChunkThread(otherClient, message);
	}

	public Map<String, ChatData> getChatMap() {
		return chatMap;
	}
	
	

}
