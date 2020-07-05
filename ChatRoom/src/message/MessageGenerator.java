package message;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class MessageGenerator {
	
	public static Message register(String name, String passwort) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("name", name);
		responseMap.put("passwort", passwort);
		
		return new Message(Command.REGISTER, responseMap);
	}
	
	public static Message login(String name, String passwort) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("name", name);
		responseMap.put("passwort", passwort);
		
		return new Message(Command.LOGIN, responseMap);
	}
	
	public static Message logout(String name) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("name", name);
		
		return new Message(Command.LOGOUT, responseMap);
	}
	
	// New Chat
	public static Message sendRequest(String requestRecipient, InetAddress senderAddress, int senderPort) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestRecipient", requestRecipient);
		responseMap.put("senderAddress", senderAddress.getHostAddress());
		responseMap.put("senderPort", Integer.toString(senderPort));
		
		return new Message(Command.SEND_REQUEST, responseMap);
	}
	
	public static Message newRequest(String requestSender) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestSender", requestSender);
		
		return new Message(Command.NEW_REQUEST, responseMap);
	}
	
	public static Message declineRequest(String requestSender) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestSender", requestSender);
		
		return new Message(Command.DECLINE_REQUEST, responseMap);
	}
	
	public static Message acceptRequest(String requestSender) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestSender", requestSender);
		
		return new Message(Command.ACCEPT_REQUEST, responseMap);
	}
	
	public static Message requestDeclined(String requestRecipient) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestRecipient", requestRecipient);
		
		return new Message(Command.REQUEST_DECLINED, responseMap);
	}
	
	
	public static Message requestAccepted(String requestRecipient) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("requestRecipient", requestRecipient);
		
		return new Message(Command.REQUEST_ACCEPTED, responseMap);
	}
	
	// Chat
	public static Message sendTextMessage(String text, String messageRecipient, String time) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("text", text);
		responseMap.put("messageRecipient", messageRecipient);
		responseMap.put("time", time);
		
		return new Message(Command.SEND_TEXT_MESSAGE, responseMap);
	}
	
	public static Message newTextMessage(String text, String messageSender, int messageNr) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("text", text);
		responseMap.put("messageSender", messageSender);
		responseMap.put("messageNr", Integer.toString(messageNr));
		
		return new Message(Command.NEW_TEXT_MESSAGE, responseMap);
	}
	
	public static Message chunkSaved(int chunkNr) {
		Map<String, String> responseMap = new HashMap<>();
//		responseMap.put("messageRecipient", messageRecipient);
		responseMap.put("chunkNr", Integer.toString(chunkNr));
		
		return new Message(Command.CHUNK_SAVED, responseMap);
	}
	
	public static Message chunkReceived(String messageSender, int chunkNr) {
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("messageSender", messageSender);
		responseMap.put("chunkNr", Integer.toString(chunkNr));
		
		return new Message(Command.CHUNK_RECEIVED, responseMap);
	}
	
	// Illegal Action
//	public static Message illegalCommand() {
//		return new Message(Command.ILLEGAL_COMMAND);
//	}

}
