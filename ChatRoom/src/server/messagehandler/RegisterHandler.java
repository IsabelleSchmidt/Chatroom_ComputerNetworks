package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;
import server.ServerTCPThread;

public class RegisterHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String userName = m.getAttributes().get("name");
		String userPasswort = m.getAttributes().get("passwort");
		
		if (data.usernameAvailable(userName)) {
			data.registerUser(userName, userPasswort);
			
			clientThread.setClientName(userName);
			data.addTCPRoutingInfo(userName, clientThread);
			data.addActiveUser(userName);			
			
			return new Message(Command.REGISTER_ACCEPTED);
			
		} else {
			return new Message(Command.REGISTER_DECLINED);
		}
	}

}
