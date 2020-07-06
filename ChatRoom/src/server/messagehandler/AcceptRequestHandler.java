package server.messagehandler;

import message.Message;
import message.MessageGenerator;
import server.ServerData;
import server.ServerTCPThread;

public class AcceptRequestHandler implements ServerRequestHandler {

	@Override
	public void handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String requestSender = m.getAttributes().get("requestSender");
		String recipientAddress = m.getAttributes().get("recipientAddress");
		String recipientPort = m.getAttributes().get("recipientPort");
		
		String requestRecipient = clientThread.getClientName();
		Message response = MessageGenerator.requestAccepted(requestRecipient, recipientAddress, recipientPort);
		
		data.getClientThread(requestSender).sendMessage(response);
	}


}
