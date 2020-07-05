package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;

public class RegisterHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data) {
		String userName = m.getAttributes().get("name");
		String userPasswort = m.getAttributes().get("passwort");
		
		if (data.usernameAvailable(userName)) {
			data.registerUser(userName, userPasswort);
			data.addActiveUser(userName);			
			
			return new Message(Command.REGISTER_ACCEPTED);
			
		} else {
			return new Message(Command.REGISTER_DECLINED);
		}
	}

}
