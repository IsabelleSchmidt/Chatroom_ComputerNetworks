package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;
import server.ServerTCPThread;

public class LogoutHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String userName = m.getAttributes().get("name");
		data.loggoutUser(userName);
		return new Message(Command.LOGEDOUT);
	}

}
