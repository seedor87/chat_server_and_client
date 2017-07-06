package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatGateway implements chat.ChatConstants {

    private static final int PORT_NUMBER = 8000;
    private PrintWriter outputToServer;
    private BufferedReader inputFromServer;
    private ScrollPane scrollPane;
    private TextFlow textFlow;

    // Establish the connection to the server.
    public ChatGateway(TextFlow textFlow, ScrollPane scrollPane) {

        this.scrollPane = scrollPane;
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.textFlow = textFlow;

        this.textFlow.setPadding(new Insets(10));

        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", PORT_NUMBER);

            // Create an output stream to send data to the server
            outputToServer = new PrintWriter(socket.getOutputStream());

            // Create an input stream to read data from the server
            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException ex) {
            Platform.runLater(() -> {
                textFlow.getChildren().add(new Text("Exception in gateway constructor: " + ex.toString() + "\n"));
                scrollPane.setVvalue(1.0);
            });
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
    public void sendLogin(String username, String password, String forum) {
        outputToServer.println(SEND_LOGIN);
        outputToServer.println(username);
        outputToServer.println(password);
        outputToServer.println(forum);
        outputToServer.flush();
    }

    public int getLogin() {
        outputToServer.println(GET_LOGIN);
        outputToServer.flush();
        int result_code = -2;
        try{
            String res = inputFromServer.readLine();
            result_code = Integer.parseInt(res);
        }
        catch (IOException ex) {
            Platform.runLater(() -> {
                textFlow.getChildren().add(new Text("Error in getLogin: " + ex.toString() + "\n"));
                scrollPane.setVvalue(1.0);
            });
        }
        return result_code;
    }

    // Send a new comment to the server.
    public void sendComment(String comment) {
        outputToServer.println(SEND_COMMENT);
        outputToServer.println(comment);
        outputToServer.flush();
    }

    public void sendFile(String path) {
        outputToServer.println(SEND_FILE);
        outputToServer.println(path);
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
            Platform.runLater(() -> {
                textFlow.getChildren().add(new Text("Error in getCommentCount: " + ex.toString() + "\n"));
                scrollPane.setVvalue(1.0);
            });
        }
        catch (NumberFormatException ex) {
            try {
                // throw out response for now
                inputFromServer.readLine();
//                textFlow.appendText(res);
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
        }
        return count;
    }

    // Fetch comment n of the transcript from the server.
    public JSONObject getComment(int n) {
        outputToServer.println(GET_COMMENT);
        outputToServer.println(n);
        outputToServer.flush();
        try {
            String message = inputFromServer.readLine();
            final JSONObject obj = new JSONObject(message);
            return obj;
        } catch (IOException ex) {
            Platform.runLater(() -> {
                textFlow.getChildren().add(new Text("Error in getComment: " + ex.toString() + "\n"));
                scrollPane.setVvalue(1.0);
            });
        } catch (JSONException ex) {
            Platform.runLater(() -> {
                textFlow.getChildren().add(new Text("Error in getComment: " + ex.toString() + "\n"));
                scrollPane.setVvalue(1.0);
            });
        }
        return new JSONObject();
    }
}
