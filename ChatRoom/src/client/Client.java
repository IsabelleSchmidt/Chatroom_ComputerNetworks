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
	public ObservableList<String> activeUser = FXCollections.observableArrayList();
	
	//TCP
	private Socket socket;
	private BufferedWriter writer;
    private BufferedReader reader;
	private Thread listenThread;
	
	// UDP
	private InetAddress clientAddress;
	private int clientPort;
	private ClientUDPThread udpThread;
	
	// GUI
	private boolean loggedout = false;
	private boolean login = false;
	private boolean register = true;

	public Client(String name, int port) {
		this.name = name;
		
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
		
		udpThread = new ClientUDPThread(name, port);
	}
	
	public void startTCP() throws UnknownHostException, IOException {
		System.out.println("startClient Methode wurde aufgerufen");
		
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
		
		System.out.println(this.name + ": send to Server - " + clientMessage.getRaw());
		writeMessage(clientMessage);
	}
	
	public void acceptRequest(String requestSender, String senderAddress, String senderPort) {
		Message clientMessage = MessageGenerator.acceptRequest(requestSender, clientAddress, clientPort);
		System.out.println(this.name + ": sent to Server - " + clientMessage.getRaw());
		
		writeMessage(clientMessage);
		newChat(requestSender, senderAddress, senderPort);
	}
	
	public void declineRequest(String requestSender) {
		Message clientMessage = MessageGenerator.declineRequest(requestSender);
		System.out.println(this.name + ": sent to Server - " + clientMessage.getRaw());
		
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
		udpThread.addConnectionData(otherClient, info);
		udpThread.addChatData(info, chatData);
		udpThread.start();
	}
	
	public void sendTextMessage(String otherClient, String text) {
		ChatMessage newMessage = new ChatMessage(this.name, text);
		EndpointInfo info = udpThread.connectionData.get(otherClient);
		udpThread.chatData.get(info).addMessage(newMessage);
		
		Message message = MessageGenerator.sendTextMessage(text, newMessage.getTime());
		udpThread.startChunkThread(message, info);
	}
	
	public void listen() {
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
                        System.out.println("Thread endet wie gew�nscht.");
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
		System.out.println(serverMessage.getRaw());
		
		switch (serverCommand) {
		case REGISTER_ACCEPTED:
			register = true;
			System.out.println("REGISTRIEREN");
			break;
		case REGISTER_DECLINED:
			register = false;
			System.out.println("REGISTRIEREN");
			break;
		case LOGIN_ACCEPTED:
			login = true;
			break;
		case LOGIN_DECLINED:
			login = false;
			break;
		case REQUEST_ACCEPTED:
			System.out.println(this.name + ": Request accepted. Start new chat.");
			String name = serverMessage.getAttributes().get("requestRecipient"); //name
			String address = serverMessage.getAttributes().get("recipientAddress"); //address
			String port = serverMessage.getAttributes().get("recipientPort");
			
			newChat(name, address, port);
			break;
		case REQUEST_DECLINED:
			System.out.println(this.name + ": Request declined.");
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
			loggedout = true;
			closeTCPSocket();
			break;
		default:
		    System.out.println("line: " + line);
		    if(!activeUser.contains(line)) {
		    	activeUser.add(line);
		    }else {
		    	activeUser.remove(line);
		    }
		    System.out.println("activeUser Client: " + activeUser);
		

		}
	}
	
	private void writeMessage(Message message) {
    	try {
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
            System.out.println("Client: Schlie�e Socket...");
            socket.close();
            listenThread = null;
        }
	}
	
	public ChatData getChatData(String name) {
		if (udpThread.chatData.containsKey(udpThread.connectionData.get(name))) {
			return udpThread.chatData.get(udpThread.connectionData.get(name));
		}
		return null;
	}
	
	public void setLoggedout(boolean loggedout) {
		this.loggedout = loggedout;
	}

	public boolean isLoggedout() {
		return loggedout;
	}
	
	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}
	
	public boolean isRegister() {
		return register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}
	
	public ObservableList<String> getActiveUser() {
		return activeUser;
	}
	
}
