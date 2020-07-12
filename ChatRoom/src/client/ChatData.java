package client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import message.ChatMessage;
import message.Chunk;
import message.Message;

/**
 * Class represents chat data with another client.
 */
public class ChatData {
	
	private String otherClient;
	private InetAddress otherAddress;
	private int otherPort;
	private ObservableList<ChatMessage> chatHistory;
	private List<Chunk> aktChunks;
	
	public ChatData(String userName, InetAddress clientAddress, int clientPort) {
		this.otherClient = userName;
		this.otherAddress = clientAddress;
		this.otherPort = clientPort;
		chatHistory = FXCollections.observableArrayList();
		aktChunks = new ArrayList<>();
	}
	
	public void addChunk(Chunk chunk) {
		aktChunks.add(chunk);
		Collections.sort(aktChunks);
		
		if (chunk.isEnd()) {
			if (aktChunks.size() == (chunk.getChunkNr() + 1)) {
				
				Message message = Message.fromChunks(aktChunks);
				System.out.println("ChatData: fromChunks() - message " + message.getRaw());
				ChatMessage chatMessage = new ChatMessage(otherClient, message.getAttributes().get("text"));
				System.out.println("ChatData: addChunk(): message received " + chatMessage.toString());
				addMessage(chatMessage);
				aktChunks.clear();
			}
		}
	}
	
	public void addMessage(ChatMessage message) {
		chatHistory.add(message);
	}

	public String getOtherClientName() {
		return otherClient;
	}

	public ObservableList<ChatMessage> getMessages() {
		System.out.println(chatHistory);
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
