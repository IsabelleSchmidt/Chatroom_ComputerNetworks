package client;

import java.util.ArrayList;
import java.util.List;

import message.ChatMessage;


public class ChatData {
	
	private String userName;
	private List<ChatMessage> chatHistory;
	
	public ChatData(String userName) {
		this.userName = userName;
		chatHistory = new ArrayList<>();
	}
	
	public void addMessage(ChatMessage message) {
		chatHistory.add(message);
	}

	public String getUserName() {
		return userName;
	}

	public List<ChatMessage> getChatHistory() {
		return chatHistory;
	}

}
