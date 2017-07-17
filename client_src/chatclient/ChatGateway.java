package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class stores the tools that control the reaction of the client to response codes from the server.
 * On construction, the chatGateway instance starts a thread that shares a buffered reader and writer with a thread on the server.
 * Each method represents an action taken by the client. The methods are each binary in implementation; each action requires a request for action followed by an execution of that action.
 * An example: the client sends a sendLogin action to send credentials to the server for vetting, and then reacts based on the result of the following getLogin. This reaction will either permit the client session to exist or deny it.
 */
public class ChatGateway implements chat.ChatConstants {

    private static final int PORT_NUMBER = 8000;
    private PrintWriter outputToServer;
    private BufferedReader inputFromServer;
    private ScrollPane scrollPane;
    private TextFlow textFlow;

    /** Establish the connection to the server.
     * Constructor of one ChatGateway instance.
     */
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

    /** start interaction with an alive? request.
     * Only usable to see if the output buffer has been successfully created.
     *
     */
    public boolean sendPing() {
        try {
            outputToServer.println(0);
            outputToServer.flush();
        }
        catch (NullPointerException ex) {
            return false;
        }
        return true;
    }

    /** Start the chat by sending in the user's handle.
     * All three area aligned in the agreed upon order on the buffer to the server.
     *
     */
    public void sendLogin(String username, String password, String forum) {
        outputToServer.println(SEND_LOGIN);
        outputToServer.println(username);
        outputToServer.println(password);
        outputToServer.println(forum);
        outputToServer.flush();
    }

    /** Send request code for success? or fail? login, immediately to follow sendLogin in practice.
     *
     * @return result_code - the special integer value of the success fo failure:
     *                      -2 for bad username / password,
     *                      -1 for bad forum req,
     *                      0 for success.
     */
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

    /** Send a new comment to the server, using request code for text-type message (synonymous with comment)
     *
     */
    public void sendComment(String comment) {
        outputToServer.println(SEND_COMMENT);
        outputToServer.println(comment);
        outputToServer.flush();
    }

    /** Variant of sendComment, sends special file-type message on wire.
     * A separate method is necessary as the server needs a different request code to coordinate a response for.
     *
     * @param path
     */
    public void sendFile(String path) {
        outputToServer.println(SEND_FILE);
        outputToServer.println(path);
        outputToServer.flush();
    }

    /** Ask the server to send us a count of how many comments are currently in the transcript.
     * This value is used to check against this client's known collection of comments and request the newest one.
     */
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
            return -1;
        }
        catch (NumberFormatException ex) {
            try {
                // throw out response for now
                inputFromServer.readLine();
                // textFlow.appendText(res);
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
        }
        return count;
    }

    /** Fetch comment n of the transcript from the server.
     * The response on the buffer must be a string that is parsed to a JSON object.
     *
     * @param n - integer index of the NEXT comment requested
     * @return JSON Object of the response string
     */
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
