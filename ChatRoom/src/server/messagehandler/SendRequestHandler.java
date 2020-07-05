package server.messagehandler;

import message.Message;
import message.MessageGenerator;
import server.ServerData;

public class SendRequestHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data) {
		String clientName = m.getAttributes().get("requestRecipient");
		String address = m.getAttributes().get("senderAddress");
		String host = m.getAttributes().get("senderPort");
		data.addRoutingInfo(clientName, address, host);
		
		//TODO: Jetzt geht die Message an den Client zurück. Es soll aber an den anderen Client weiterleitet werden
		return MessageGenerator.newRequest(clientName);
	}


}
