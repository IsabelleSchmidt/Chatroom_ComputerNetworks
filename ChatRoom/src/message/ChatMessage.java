package message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage implements Comparable<ChatMessage> {
	
	String userName;
	String text;
	String time;
	
	public ChatMessage(String userName,	String text) {
		this.userName = userName;
		this.text = text;

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		this.time = formatter.format(date);
	}
	
	@Override
	public String toString() {
		return String.format("%s\n\n%s\n", userName, text, time);
	}

	public String getUserName() {
		return userName;
	}

	public String getText() {
		return text;
	}

	public String getTime() {
		return time;
	}

	@Override
	public int compareTo(ChatMessage other) {
		return 0;
	}

}
