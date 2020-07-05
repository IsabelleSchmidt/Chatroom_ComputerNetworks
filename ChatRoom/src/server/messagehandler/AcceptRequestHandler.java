package server.messagehandler;

import message.Message;
import message.MessageGenerator;
import server.ServerData;

public class AcceptRequestHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data) {
		return MessageGenerator.requestAccepted(m.getAttributes().get("requestSender"));
	}


}
