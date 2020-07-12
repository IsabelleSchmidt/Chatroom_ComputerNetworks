package server.messagehandler;

import message.Command;
import message.Message;
import message.MessageGenerator;
import server.ServerData;
import server.ServerTCPThread;

public class LogoutHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String userName = m.getAttributes().get("name");
		data.loggoutUser(userName);
		
		//if current user was logged out, send other users current user's status
		Message newUserMessage = MessageGenerator.userOffline(userName);
		data.removeTCPRoutingInfo(userName);
		data.getRoutingTableTCP().values()
			.stream()
			.forEach(u -> u.sendMessage(newUserMessage));
		
		return new Message(Command.LOGEDOUT);
	}

}
