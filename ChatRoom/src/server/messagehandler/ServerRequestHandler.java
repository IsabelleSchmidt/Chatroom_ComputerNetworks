package server.messagehandler;

import message.Message;
import server.ServerData;
import server.ServerTCPThread;

public interface ServerRequestHandler {
	void handle(Message m, ServerData data, ServerTCPThread clientThread);
}
