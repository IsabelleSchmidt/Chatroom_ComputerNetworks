package message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import application.Config;

public class Message {
	private Command command;
	private Map<String, String> attributes;
	private String raw;
	
	public Message(String line) {
		this.raw = line;
		this.attributes = new HashMap<>();
		
		String[] components = line.split(";");
		
		if (components.length >= 1) {
			try {
				command = Command.valueOf(components[0]);
			} catch (Exception e) {
				command = Command.ILLEGAL_COMMAND;
			}
		}
		
		if (components.length >= 2) {
			try {
				String attributeLine = components[1];
				for (String pair : attributeLine.split(",")) {
					String[] pairComponents = pair.split("=");
					attributes.put(pairComponents[0], pairComponents[1]);
				}
			} catch (Exception e) {
				command = Command.ILLEGAL_COMMAND;
			}
		}
		
	}
	
	public Message(Command command) {
		this.command = command;
		this.raw = serialize();
	}
	
	public Message(Command command, Map<String, String> attributes) {
		this.command = command;
		this.attributes = attributes;
		this.raw = serialize();
	}
	
	public Chunk[] chunk() {
        int chunkCount = (int) Math.ceil(raw.length() / (double) Config.CHUNK_CONTENT_SIZE);
        Chunk[] chunks = new Chunk[chunkCount];
	
        for (int i = 0; i < chunkCount; i++) {
            int start = i*Config.CHUNK_CONTENT_SIZE;
            int end = start + Config.CHUNK_CONTENT_SIZE;
            String chunkString = raw.substring(start, end <= raw.length() ? end : raw.length());
            boolean isLast = i == chunkCount - 1;
            chunks[i] = new Chunk(i, isLast, chunkString);
        }
	
        return chunks;
	
    }
	
	public static Message fromChunks(List<Chunk> chunks) {
        String raw = "";
        for (Chunk chunk : chunks) {
            raw += chunk.getContent();
        }
        
        return new Message(raw);
    }
	
	public String serialize() {
		if (attributes != null) {
			return String.format("%s;%s", command, attributes.entrySet().stream()
					.map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
					.collect(Collectors.joining(",")));
		}
		return command.name();
		
	}
	
//	public static Message parse(String raw) {
//        if (raw == null || raw.isEmpty()) {
//            return null;
//        }
//        
//        String[] components = raw.split(";");
//        Command type = Command.valueOf (components[0]);
//        HashMap<String, String> body = new HashMap<>();
//        
//        if (components.length >= 2) {
//            for (String keyValuePair : components[1].split(",")) {
//                String[] kvComponents = keyValuePair.split("=");
//                body.put(kvComponents[0], kvComponents[1]);
//            }
//        }
//        return new Message(type, body);
//
//    }
	
	

	public Command getCommand() {
		return command;
	}

	public String getRaw() {
		return raw;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}
	
}
