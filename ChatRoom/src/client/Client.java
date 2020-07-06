package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import message.ChatMessage;
import message.Command;
import message.Message;
import message.MessageGenerator;
import server.EndpointInfo;


public class Client {

	private InetAddress serverAddress;
	private int serverPort = 8888;
	private String name;
	
	//TCP
	private Socket socket;
//	private BufferedWriter writer;
//    private BufferedReader reader;
	private DataInputStream inStream;
	private DataOutputStream outStream;

	private boolean clientOnline;
	private Thread listenThread;
	
	// UDP
	private InetAddress clientAddress;
	private int clientPort;
	private ClientUDPThread udpThread;
	
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
		
		udpThread = new ClientUDPThread(name);
		listen();
	}
	
	public void startTCP() throws UnknownHostException, IOException {
		System.out.println("startClient Methode wurde aufgerufen");
		
		// Socket, Output, Input, Reader
		try {
			socket = new Socket(serverAddress.getHostAddress(), serverPort);
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
//			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
		
		clientOnline = true;
		listen();
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
		
		clientOnline = true;
		listen();
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
		
		clientOnline = false;
		closeTCPSocket();
		return false;
	}
	
	public boolean sendChatRequest(String otherClientName) {
		Message clientMessage;
		Message serverMessage;
		
		clientMessage = MessageGenerator.sendRequest(otherClientName, clientAddress, clientPort);
		
		System.out.println(this.name + ": send to Server - " + clientMessage.getRaw());
		try {
			outStream.writeUTF(clientMessage.getRaw());
			outStream.flush();
			
			serverMessage = new Message(inStream.readUTF());
			System.out.println(this.name + ": from Server - " + serverMessage.getRaw());
			
			if (serverMessage.getCommand() == Command.REQUEST_ACCEPTED) {
				System.out.println(this.name + ": Request accepted. Start new chat.");
				String name = serverMessage.getAttributes().get("requestRecipient"); //name
				String address = serverMessage.getAttributes().get("recipientAddress"); //address
				String port = serverMessage.getAttributes().get("recipientPort");
				
				newChat(name, address, port);
				return true;
				
			} else if (serverMessage.getCommand() == Command.REQUEST_DECLINED) {
				System.out.println(this.name + ": Request declined.");
				return false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	public void acceptRequest(String requestSender) {
		Message clientMessage = MessageGenerator.acceptRequest(requestSender, clientAddress, clientPort);
		System.out.println(this.name + ": sent to Server - " + clientMessage.getRaw());
		
		try {
			outStream.writeUTF(clientMessage.serialize());
			outStream.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		newChat(requestSender, clientAddress, serverPort);
		
	}

	private void newChat(String otherClient, String clientAddress, String clientPort) {
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
		udpThread.start();
		udpThread.addConnectionData(otherClient, info);
		udpThread.addChatData(info, chatData);
	}
	
	private void newChat(String otherClient, InetAddress clientAddress, int clientPort) {
		// Create chat data
		ChatData chatData = new ChatData(otherClient, clientAddress, clientPort);
		EndpointInfo info = new EndpointInfo(clientAddress, clientPort);
		
		// Start new chat thread
		udpThread.start();
		udpThread.addConnectionData(otherClient, info);
		udpThread.addChatData(info, chatData);

	}
	
	public void sendTextMessage(String otherClient, String text) {
		ChatMessage newMessage = new ChatMessage(this.name, text);
		EndpointInfo info = udpThread.connectionData.get(otherClient);
		udpThread.chatData.get(info).addMessage(newMessage);
		
		Message message = MessageGenerator.sendTextMessage(text, newMessage.getTime());
		udpThread.startChunkThread(message, info);
	}
	
	public void listen() {
        
        listenThread = new Thread(() -> {
            while (clientOnline) {
                try {
                	Message clientMessage = new Message(inStream.readUTF());
            		System.out.println(this.name + ": from Server - " + clientMessage.getRaw());
            		
                    handleMessage(clientMessage);
                    
                } catch (SocketException e) {
                    if (!clientOnline) {
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
	
	public boolean handleMessage(Message serverMessage) {
		Command serverCommand = serverMessage.getCommand();
		
		switch (serverCommand) {
		case NEW_REQUEST:
			acceptRequest(serverMessage.getAttributes().get("requestSender"));
			break;
		default:
			break;
		}
		return true;
	}
	
	public ChatData getChatData(String name) {
		if (udpThread.chatData.containsKey(udpThread.connectionData.get(name))) {
			return udpThread.chatData.get(udpThread.connectionData.get(name));
		}
		return null;
	}
	

}
