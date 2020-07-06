package server.messagehandler;

import java.io.DataOutputStream;
import java.io.IOException;

import message.Message;
import message.MessageGenerator;
import server.ServerData;
import server.ServerTCPThread;

public class SendRequestHandler implements ServerRequestHandler {

	@Override
	public void handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String recipientName = m.getAttributes().get("requestRecipient");
		String address = m.getAttributes().get("senderAddress");
		String host = m.getAttributes().get("senderPort");
		
		// Request Transfer
		Message response = MessageGenerator.newRequest(recipientName, address, host);
		data.getClientThread(recipientName).sendMessage(response);
		
	}


}
