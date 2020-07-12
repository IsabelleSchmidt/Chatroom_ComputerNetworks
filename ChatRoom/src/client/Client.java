package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import message.ChatMessage;
import message.Command;
import message.Message;
import message.MessageGenerator;
import server.EndpointInfo;


public class Client {

	private InetAddress serverAddress;
	private int serverPort = 8888;
	private String name;
	private ObservableList<String> activeUser = FXCollections.observableArrayList();
	private ObservableList<String> chatPartners = FXCollections.observableArrayList();
	
	//TCP
	private Socket socket;
	private BufferedWriter writer;
    private BufferedReader reader;
	private Thread listenThread;
	
	// UDP
	private InetAddress clientAddress;
	private int clientPort;
	private ClientUDPService udpService;
	
	// GUI
	public SimpleBooleanProperty loggedOutProperty;
	public SimpleBooleanProperty loggedInProperty;
	public SimpleBooleanProperty registeredProperty;

	public SimpleBooleanProperty logInFailed;
	public SimpleBooleanProperty registerFailed;


	public Client(String name, int port) {
		this.name = name;
		this.loggedOutProperty = new SimpleBooleanProperty();
		this.loggedInProperty = new SimpleBooleanProperty();
		this.registeredProperty = new SimpleBooleanProperty();

		this.logInFailed = new SimpleBooleanProperty();
		this.registerFailed = new SimpleBooleanProperty();
		
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
		
		udpService = new ClientUDPService(name, port);
	}
	
	public void startTCP() throws UnknownHostException, IOException {
		System.out.println("Client: startTCP()");
		
		// Socket, Output, Input, Reader
		try {
			socket = new Socket(serverAddress.getHostAddress(), serverPort);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			listen();

		} catch (Exception e) {
			System.out.println("Exception" + e);
		}
	}

	public void registrier(String name, String passwort) throws IOException {
		Message clientMessage = MessageGenerator.register(name, passwort);
		writeMessage(clientMessage);
	}

	public void login(String name, String passwort) throws IOException {
		Message clientMessage = MessageGenerator.login(name, passwort);
		writeMessage(clientMessage);
	}
	
	public void logout(String name) {
		Message clientMessage = MessageGenerator.logout(name);
		writeMessage(clientMessage);
	}
	
	public void sendChatRequest(String otherClientName) {
		Message clientMessage = MessageGenerator.sendRequest(otherClientName, clientAddress, clientPort);
		writeMessage(clientMessage);
	}
	
	public void acceptRequest(String requestSender, String senderAddress, String senderPort) {
		Message clientMessage = MessageGenerator.acceptRequest(requestSender, clientAddress, clientPort);
		writeMessage(clientMessage);
		newChat(requestSender, senderAddress, senderPort);
	}
	
	public void declineRequest(String requestSender) {
		Message clientMessage = MessageGenerator.declineRequest(requestSender);
		writeMessage(clientMessage);
	}

	private void newChat(String otherClient, String clientAddress, String clientPort) {
		System.out.println(String.format("%s: start new chat with %s (port:%s).", this.name, otherClient, clientPort));
		
		int port = Integer.parseInt(clientPort);
		InetAddress address = null;
		try {
			address = InetAddress.getByName(clientAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Create chat data
		ChatData chatData = new ChatData(otherClient, address, port);
		EndpointInfo info = new EndpointInfo(address, port);
		
		// Start new chat thread
		udpService.addConnectionData(otherClient, info);
		udpService.addChatData(info, chatData);
		chatPartners.add(otherClient);
		
		if (!udpService.isSocketOn()) {
			udpService.start();
		}

	}
	
	public void sendTextMessage(String otherClient, String text) {
		ChatMessage newMessage = new ChatMessage("ich", text);
		EndpointInfo info = udpService.connectionData.get(otherClient);
		udpService.chatData.get(info).addMessage(newMessage);
		
		Message message = MessageGenerator.sendTextMessage(text, newMessage.getTime());
		udpService.startChunkThread(message, info);
	}
	
	public void listen() {
		System.out.println("Client: listen()");
		
		if (listenThread != null) {
            listenThread.interrupt();
            listenThread = null;
        }
    	
        listenThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    final String raw = reader.readLine();
                    if (Thread.interrupted()) {
                        break;
                    }
                    if (raw == null) {
                        break;
                    }
                    handleMessage(raw);
                    
                } catch (SocketException e) {
                    if (Thread.interrupted()) {
                        System.out.println("Thread endet wie gewünscht.");
                    } else {
                    	System.out.println("Client: Server is not available...");
                    }
                    break;
                } catch (IOException e) {
                	System.out.println("Client: Server is not available...");
                    break;
                }
            }
        });
        
        listenThread.start();
	}
	
	public void handleMessage(String line) throws IOException {
		Message serverMessage = new Message(line);
		Command serverCommand = serverMessage.getCommand();
		System.out.println(this.name + ": from server - " + serverMessage.getRaw());
		
		switch (serverCommand) {
		case REGISTER_ACCEPTED:
			registeredProperty.set(true);
			break;
		case REGISTER_DECLINED:
			registerFailed.set(true);
			break;
		case LOGIN_ACCEPTED:
			loggedInProperty.set(true);
			break;
		case LOGIN_DECLINED:
			logInFailed.set(true);
			break;
		case REQUEST_ACCEPTED:
			System.out.println(this.name + ": Request accepted. Start new chat.");
			String name = serverMessage.getAttributes().get("requestRecipient"); //name
			String address = serverMessage.getAttributes().get("recipientAddress"); //address
			String port = serverMessage.getAttributes().get("recipientPort");
			
			newChat(name, address, port);
			break;
		case REQUEST_DECLINED:
			// TODO: GUI aendern
			break;
		case NEW_REQUEST:
			// TODO: Auf GUI die neue Anfrage anzeigen ->
			// User: Annehmen -> acceptRequest(), ablehnen -> declineRequest()
			// Momentan wird die Anfrage automatisch angenommen:
			String requestSender = serverMessage.getAttributes().get("requestSender");
			String senderAddress = serverMessage.getAttributes().get("senderAddress");
			String senderPort = serverMessage.getAttributes().get("senderPort");
			acceptRequest(requestSender, senderAddress, senderPort);
			break;
		case LOGEDOUT:
			loggedOutProperty.set(true);
			udpService.stop(); // close udp socket, interrupt listener
			closeTCPSocket();
			break;
		case USER_ONLINE:
			activeUser.add(serverMessage.getAttributes().get("name"));
			System.out.println(this.name + ": user online: " + serverMessage.getAttributes().get("name"));
			break;
		case USER_OFFLINE:
			activeUser.remove(serverMessage.getAttributes().get("name"));
			if (chatPartners.contains(serverMessage.getAttributes().get("name"))) {
				chatPartners.remove(serverMessage.getAttributes().get("name"));
			}
			System.out.println(this.name + ": user offline: " + serverMessage.getAttributes().get("name"));
			break;
		default:
		    break;
		}
	}
	
	private void writeMessage(Message message) {
    	try {
    		System.out.println(this.name + ": send to server - " + message.getRaw());
	        writer.write(message.getRaw() + "\n");
	        writer.flush();
    	} catch (IOException e) {
    		System.out.println(this.name + "Client: Server is not available...");
    		try {
				closeTCPSocket();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
    }
	
	
	public void closeTCPSocket() throws IOException {
		if (listenThread != null) {
            listenThread.interrupt();
            System.out.println("Client: Schliesse Socket...");
            socket.close();
            listenThread = null;
        }
	}
	
	public ChatData getChatData(String name) {
		if (udpService.chatData.containsKey(udpService.connectionData.get(name))) {
			return udpService.chatData.get(udpService.connectionData.get(name));
		}
		return null;
	}
	
	public ObservableList<String> getActiveUser() {
		return activeUser;
	}

	public ObservableList<String> getChatPartners() {
		return chatPartners;
	}
	
}
