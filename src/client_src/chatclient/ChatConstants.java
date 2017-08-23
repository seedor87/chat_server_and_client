package chatclient;

/**
 * This file holds the constants thate represent the signal codes that the client and the server agree to follow
 * In short, each is a case in a switch statement that the server and client use to maintain the contract of callbacks.
 */
public interface ChatConstants {
    public static int SEND_LOGIN = 1;
    public static int GET_LOGIN = 2;
    public static int SEND_COMMENT = 3;
    public static int GET_COMMENT_COUNT = 4;
    public static int GET_COMMENT = 5;
    public static int SEND_FILE = 6;
}
