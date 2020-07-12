package server;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
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
	
	public ServerTCPThread(Socket clientSocket, ServerData serverData) {
		this.serverData = serverData;
		this.clientSocket = clientSocket;
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
		System.out.println("SERVER: from client - " + clientMessage.getRaw());

		if (clientCommand == Command.LOGIN || clientCommand == Command.REGISTER || clientCommand == Command.LOGOUT) {
			ServerMessageHandler messageHandler = messageHandlerMap.get(clientMessage.getCommand());
			
			if (messageHandler != null) {
				Message response = messageHandler.handle(clientMessage, serverData, this);
				
				if (response != null) {
					sendMessage(response);
					
					if (response.getCommand() == Command.LOGEDOUT) {
						// close this socket, stop this thread
						closeTCPSocket();
						this.interrupt();
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
			System.out.println("SERVER: send to client - " + m.getRaw());
			writer.write(m.getRaw() + "\n");
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