package client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import message.ChatMessage;
import message.Chunk;
import message.Message;


public class ChatData {
	
	private String otherClient;
	private InetAddress otherAddress;
	private int otherPort;
	private List<ChatMessage> chatHistory;
	private List<Chunk> aktChunks;
	
	public ChatData(String userName, InetAddress clientAddress, int clientPort) {
		this.otherClient = userName;
		this.otherAddress = clientAddress;
		this.otherPort = clientPort;
		chatHistory = new ArrayList<>();
	}
	
	public void addChunk(Chunk chunk) {
		aktChunks.add(chunk);
		Collections.sort(aktChunks);
		
		if (chunk.isEnd()) {
			if (aktChunks.size() == (chunk.getChunkNr() + 1)) {
				Message message = Message.fromChunks((Chunk[]) aktChunks.toArray());
				ChatMessage chatMessage = new ChatMessage(otherClient, message.getAttributes().get("text"));
				addMessage(chatMessage);
			}
		}
	}
	
	public void addMessage(ChatMessage message) {
		chatHistory.add(message);
	}

	public String getOtherClientName() {
		return otherClient;
	}

	public List<ChatMessage> getChatHistory() {
		return chatHistory;
	}

	public String getOtherClient() {
		return otherClient;
	}

	public InetAddress getOtherAddress() {
		return otherAddress;
	}

	public int getOtherPort() {
		return otherPort;
	}

}
