package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;

public class LoginHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data) {
		String userName = m.getAttributes().get("name");
		String userPasswort = m.getAttributes().get("passwort");
		
		if (!data.login(userName, userPasswort)) {
			return new Message(Command.LOGIN_DECLINED);
		} else {
			data.addActiveUser(userName);
			return new Message(Command.LOGIN_ACCEPTED);
		}
	}



}
