package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.Command;
import message.Message;
import server.messagehandler.AcceptRequestHandler;
import server.messagehandler.DeclineRequestHandler;
import server.messagehandler.LoginHandler;
import server.messagehandler.LogoutHandler;
import server.messagehandler.ServerMessageHandler;
import server.messagehandler.ChunkReceivedHandler;
import server.messagehandler.RegisterHandler;
import server.messagehandler.SendRequestHandler;
import server.messagehandler.SendTextMessageHandler;


public class Server {
	
	private Map<Command, ServerMessageHandler> handlerMap;
	private ServerData serverData;
	private final int SERVERPORT = 8888;
	
	// TCP
//	private ServerTCPThread sct = new ServerTCPThread();
	private ServerSocket serverSocket;
	private boolean tcpSocketOn = true;
	private boolean serverOn = true;

	// UDP
	private ServerUDPThread chatThread;

	public Server() {
		handlerMap = new HashMap<>();
		serverData = new ServerData();
		try {
			serverSocket = new ServerSocket(this.SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		registerHandler();
	}

	public void start() {
		chatThread = new ServerUDPThread(SERVERPORT);
		chatThread.start();
		System.out.println("Server ist gestartet.");
		
		new Thread() {
			
			@Override
			public void run() {
				while (serverOn) {
					try {
						Socket socket = serverSocket.accept();
						System.out.println("Client verbindet sich.....");

						final Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {
								DataInputStream inStream;
								DataOutputStream outStream;

								try {
									inStream = new DataInputStream(socket.getInputStream());
									outStream = new DataOutputStream(socket.getOutputStream());

									while(tcpSocketOn) {
										handleRequest(socket, inStream, outStream);
									}
				
									try {
										inStream.close();
										outStream.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						thread.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}.start();
		
	}

	public void handleRequest(final Socket socket, final DataInputStream inStream, final DataOutputStream outStream) {
		Message clientMessage = null; 

		try {
			clientMessage = new Message(inStream.readUTF());
			
			ServerMessageHandler handler = handlerMap.get(clientMessage.getCommand());
			
			if (handler != null) {
				Message response = handler.handle(clientMessage, serverData);
				if (response != null) {
					System.out.println("Server: " + response.serialize());
					outStream.writeUTF(response.serialize());
					outStream.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeTCPSocket() {
//        if (tcpSocketOn == false) {
		tcpSocketOn = false;
            try {
            	System.out.println("Schliesse Server Socket...");
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
//	}
	
	public void stop() {
		serverOn = false;
	}
	
	public void registerHandler() {
		handlerMap.put(Command.REGISTER, new RegisterHandler());
		handlerMap.put(Command.LOGIN, new LoginHandler());
		handlerMap.put(Command.LOGOUT, new LogoutHandler());
		handlerMap.put(Command.SEND_REQUEST, new SendRequestHandler());
		handlerMap.put(Command.DECLINE_REQUEST, new DeclineRequestHandler());
		handlerMap.put(Command.ACCEPT_REQUEST, new AcceptRequestHandler());
		handlerMap.put(Command.SEND_TEXT_MESSAGE, new SendTextMessageHandler());
		handlerMap.put(Command.CHUNK_RECEIVED, new ChunkReceivedHandler());
	}

	public ServerData getServerData() {
		return serverData;
	}
	
	

}
