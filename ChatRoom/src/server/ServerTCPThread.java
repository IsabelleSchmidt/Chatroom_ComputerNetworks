package server;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import message.Command;
import message.Message;
import server.messagehandler.AcceptRequestHandler;
import server.messagehandler.DeclineRequestHandler;
import server.messagehandler.LoginHandler;
import server.messagehandler.LogoutHandler;
import server.messagehandler.RegisterHandler;
import server.messagehandler.SendRequestHandler;
import server.messagehandler.ServerMessageHandler;
import server.messagehandler.ServerRequestHandler;

/**
 * 
 * TCP Thread pro Client
 *
 */
public class ServerTCPThread extends Thread {
	
	private String clientName;
	private Socket clientSocket;
	private ServerData serverData;
	private boolean tcpSocketOn = true;
	
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private Map<Command, ServerMessageHandler> messageHandlerMap;
	private Map<Command, ServerRequestHandler> requestHandlerMap;
	private List<Socket> allClient = new ArrayList<>();
	
	private String login;
	private List<String> activeUser = new ArrayList<>();
	
	public ServerTCPThread(Socket clientSocket, ServerData serverData, List<Socket> all) {
		this.serverData = serverData;
		this.clientSocket = clientSocket;
		this.allClient = all;
		messageHandlerMap = new HashMap<>();
		requestHandlerMap = new HashMap<>();
		registerHandler();
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public String getClientName() {
		return clientName;
	}

	@Override
	public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			
				while(tcpSocketOn) {
					final String line;
					
					line = reader.readLine();
					if (line == null) {
						break;
					}
					handleRequest(line);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void handleRequest(String line) throws IOException {
		Message clientMessage = new Message(line);
		
		Command clientCommand = clientMessage.getCommand();
		System.out.println("clientCommand: " + clientCommand);
		if (clientCommand == Command.LOGIN || clientCommand == Command.REGISTER || clientCommand == Command.LOGOUT) {
			ServerMessageHandler messageHandler = messageHandlerMap.get(clientMessage.getCommand());
			
			if (messageHandler != null) {
				Message response = messageHandler.handle(clientMessage, serverData, this);
				System.out.println("SERVER: " + response.getRaw());
				
				if (response != null) {
					this.login = clientMessage.getAttributes().get("name");
					
					List<String> activeUserList = serverData.getActiveUser();
					System.out.println("activeUser: " + activeUserList);
					
					System.out.println("clientMessage.getCommand: " + clientMessage.getCommand());
					
						//send current user all other online login 
						for(String s : activeUserList) {
							if(s != null) {
								if(!login.equals(s)) {		//damit eigener Name nicht angezeigt wird
									System.out.println("user: " + s);
									activeUser.add(s);
									String msg2 = s;
									send(msg2);
								}
							}
						}
					
					//send other online users current user's status
					String onlineMsg = login;
					for(int i = 0; i < activeUserList.size(); i++) {
						if(!login.equals(activeUserList.get(i))) {		//damit eigener Name nicht angezeigt wird
							System.out.println("onlineMsg: " + onlineMsg);
							sendAll(onlineMsg, allClient.get(i));
						}
					}

					sendMessage(response);
					
					if (response.getCommand() == Command.LOGEDOUT) {
						closeTCPSocket();
					}
				}
			}
		} else if (clientCommand == Command.SEND_REQUEST || clientCommand == Command.ACCEPT_REQUEST || clientCommand == Command.DECLINE_REQUEST) {
			ServerRequestHandler requestHandler = requestHandlerMap.get(clientMessage.getCommand());
			requestHandler.handle(clientMessage, serverData, this);
		}	
	}
	
	public synchronized void sendMessage(Message m) {
		try {
			System.out.println("Server: " + m.getRaw());
			writer.write(m.getRaw() + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void sendAll(String m, Socket s) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			//System.out.println("Server: " + m.getRaw());
			writer.write(m + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void send(String m) {
		try {
//			System.out.println("Servermessage: " + m);
			writer.write(m + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private void closeTCPSocket() {
		tcpSocketOn = false;
        System.out.println("Schliesse TCP Socket...");
        try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void registerHandler() {
		messageHandlerMap.put(Command.REGISTER, new RegisterHandler());
		messageHandlerMap.put(Command.LOGIN, new LoginHandler());
		messageHandlerMap.put(Command.LOGOUT, new LogoutHandler());
		
		requestHandlerMap.put(Command.SEND_REQUEST, new SendRequestHandler());
		requestHandlerMap.put(Command.DECLINE_REQUEST, new DeclineRequestHandler());
		requestHandlerMap.put(Command.ACCEPT_REQUEST, new AcceptRequestHandler());
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
	
}