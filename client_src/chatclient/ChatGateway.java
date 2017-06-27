package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class ChatGateway implements chat.ChatConstants {

    private PrintWriter outputToServer;
    private BufferedReader inputFromServer;
    private TextArea textArea;
    private boolean success;

    // Establish the connection to the server.
    public ChatGateway(TextArea textArea) {
        this.textArea = textArea;
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8000);

            // Create an output stream to send data to the server
            outputToServer = new PrintWriter(socket.getOutputStream());

            // Create an input stream to read data from the server
            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Exception in gateway constructor: " + ex.toString() + "\n"));
        }
    }
    //start interaction with an alive? request
    public boolean ping() {
        try {
            outputToServer.println("0");
            outputToServer.flush();
        }
        catch (NullPointerException ex) {
            return false;
        }
        return true;
    }

    // Start the chat by sending in the user's handle.
    public void sendHandle(String username, String password, String forum) {
        outputToServer.println(SEND_HANDLE);
        outputToServer.println(username);
        outputToServer.println(password);
        outputToServer.println(forum);
        outputToServer.flush();
    }

    public int getHandle() {
        outputToServer.println(GET_HANDLE);
        outputToServer.flush();
        int result_code = -2;
        try{
            String res = inputFromServer.readLine();
            result_code = Integer.parseInt(res);
        }
        catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Error in getHandle: " + ex.toString() + "\n"));
        }
        return result_code;
    }

    // Send a new comment to the server.
    public void sendComment(String comment) {
        outputToServer.println(SEND_COMMENT);
        outputToServer.println(comment);
        outputToServer.flush();
    }

    // Ask the server to send us a count of how many comments are
    // currently in the transcript.
    public int getCommentCount() {
        outputToServer.println(GET_COMMENT_COUNT);
        outputToServer.flush();
        String res = null;
        int count = 0;
        try {
            res = inputFromServer.readLine();
            count = Integer.parseInt(res);
            if (count < 0) {
                return 0;
            }
        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Error in getCommentCount: " + ex.toString() + "\n"));
        }
        catch (NumberFormatException ex) {
            try {
                // throw out response for now
                inputFromServer.readLine();
//                textArea.appendText(res);
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
        }
        return count;
    }

    // Fetch comment n of the transcript from the server.
    public String getComment(int n) {
        outputToServer.println(GET_COMMENT);
        outputToServer.println(n);
        outputToServer.flush();
        String comment = "";
        try {
            comment = inputFromServer.readLine();
        } catch (IOException ex) {
            Platform.runLater(() -> textArea.appendText("Error in getComment: " + ex.toString() + "\n"));
        }
        return comment;
    }
}
