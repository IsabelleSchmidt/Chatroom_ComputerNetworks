package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;

public class LogoutHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data) {
		String userName = m.getAttributes().get("name");
		data.loggoutUser(userName);
		return new Message(Command.LOGEDOUT);
	}

}
