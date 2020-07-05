package server.messagehandler;

import message.Message;
import server.ServerData;

public interface ServerMessageHandler {
	Message handle(Message m, ServerData data);
}
