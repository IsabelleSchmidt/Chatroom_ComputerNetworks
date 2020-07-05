package application;

public class Config {
    public static final int ECHO_MESSAGE_SIZE = 200;
    public static final int CHUNK_CONTENT_SIZE = 10;
    public static final int HEADER_SIZE = "CHUNK|000|0|".length() + 1; // +1 for \n

    public static final boolean VERBOSE = false;
    public static final double NETWORK_ERROR_PROBABILITY = 0.03;
    public static final long TIMEOUT_MS = 500;

    public static final int RECEIVE_BUFFER_LENGTH = HEADER_SIZE + CHUNK_CONTENT_SIZE;
}
