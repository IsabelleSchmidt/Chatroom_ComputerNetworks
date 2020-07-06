package server.messagehandler;

import message.Message;
import message.MessageGenerator;
import server.ServerData;
import server.ServerTCPThread;

public class DeclineRequestHandler implements ServerRequestHandler {

	@Override
	public void handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String requestSender = m.getAttributes().get("requestSender");
		String requestRecipient = clientThread.getClientName();
		
		Message response = MessageGenerator.requestDeclined(requestRecipient);
		
		data.getClientThread(requestSender).sendMessage(response);
	}


}
