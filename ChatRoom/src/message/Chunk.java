package message;

public class Chunk implements Comparable<Chunk>{
    private String content;
    private int chunkNr;
    private boolean isEnd;

    public Chunk(int chunkNr, boolean isEnd, String content) {
        this.content = content;
        this.chunkNr = chunkNr;
        this.isEnd = isEnd;
    }

    @Override
    public String toString() {
        String str = String.format("CHUNK|%s|%d|%s\n", String.format("%03d", chunkNr), isEnd ? 1 : 0, content);
        return str;
    }

    public static boolean isChunk(String packetStr) {
        return packetStr.startsWith("CHUNK");
    }

    public static Chunk parse(String packetStr) {
        String[] dataComponents = packetStr.split("\\|"); // escape meta char | (regex)
        return new Chunk(Integer.parseInt(dataComponents[1]), dataComponents[2].equals("1"), dataComponents[3]);
    }

    public String getContent() {
        return content;
    }


    public boolean isEnd() {
        return isEnd;
    }

	public int getChunkNr() {
		return chunkNr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNr;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (chunkNr != other.chunkNr)
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Chunk otherChunk) {
		if (this.chunkNr < otherChunk.getChunkNr()) {
			return -1;
		} else if (this.chunkNr == otherChunk.getChunkNr()) {
			return 0;
		} else {
			return 1;
		}
	}

}
