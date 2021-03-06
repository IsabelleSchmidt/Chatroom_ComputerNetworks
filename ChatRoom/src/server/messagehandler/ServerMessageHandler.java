package server.messagehandler;

import message.Message;
import server.ServerData;
import server.ServerTCPThread;

public interface ServerMessageHandler {
	Message handle(Message m, ServerData data, ServerTCPThread clientThread);
}
