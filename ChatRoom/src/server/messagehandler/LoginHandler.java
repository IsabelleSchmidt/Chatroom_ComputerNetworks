package server.messagehandler;

import message.Command;
import message.Message;
import server.ServerData;
import server.ServerTCPThread;

public class LoginHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String userName = m.getAttributes().get("name");
		String userPasswort = m.getAttributes().get("passwort");
		
		if (!data.login(userName, userPasswort)) {
			return new Message(Command.LOGIN_DECLINED);
		} else {
			data.addActiveUser(userName);
			
			clientThread.setClientName(userName);
			data.addTCPRoutingInfo(userName, clientThread);
			return new Message(Command.LOGIN_ACCEPTED);
		}
	}



}
