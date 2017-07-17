/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 * This class responds to user interaction and governs the server database interaction and interaction with clients in accordance with request codes sent from the client.
 */
public class FXMLDocumentController implements Initializable {

    private int clientNo = 0;                   // field that tracks the client-independent id of the clients that request access.
    private TranscriptMap transcriptMap;        // the transcript map that stores the state of the messages keyed by their forum while the server is active.

    private static final int PORT_NUMBER = 8000;    // the fixed value for the port that the server listens on

    @FXML
    private TextArea textArea;                  // text are for the server's debug messages.

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        transcriptMap = new TranscriptMap();    // Init transcriptMap to hold the state of each forum's transcript as it was left

        this.textArea.setEditable(false);

        Platform.runLater( () -> {
            textArea.appendText("Server Started at: " + DateTimeUtils.getCurrentTimeStamp() + "\n");
        });

        new Thread( () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);

                while (true) {
                    Socket socket = serverSocket.accept();

                    clientNo++;

                    Platform.runLater( () -> {
                        textArea.appendText("Starting thread for Client #" + clientNo + " at " + new Date() + '\n');
                    });

                    new Thread(new HandleAClient(socket,textArea, clientNo)).start();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}

/**
 * This class is used to dispatch the purposed threads to the clients that are making requests.
 * each thread that is run has cases that correspond to the appropriate response for the request code.
 * The client side interface is in TranscriptCheck.java, for more info
 */
class HandleAClient implements Runnable, chat.ChatConstants {

    private Socket socket;                  // A connected socket
    private Transcript transcript;          // Reference to forum specific transcript, taken from transcript map
    private TextArea textArea;              // reference to text area of SERVER
    private String handle;                  // the username of the user that has connected to this thread.
    private HashMap<String, String> forum;  // the forum name, id, pair that is used as reference in db queries.
    private int clientNo;                   // server side client id

    /** Enum for the specialized connection codes on login request */
    private enum connection_code {
        CRED_FAIL, FORUM_FAIL, SUCCESS
    }
    private connection_code logged_in;

    /** construct client handler */
    public HandleAClient(Socket socket, TextArea textArea, int clientNo) {

        this.socket = socket;
        this.textArea = textArea;
        this.clientNo = clientNo;
        this.logged_in = null;
        this.transcript = null;
    }

    /** run thread */
    public void run() {
        int request = 0;
        try {
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());

            // Continuously serve the client
            while (true) {
              // Receive request code from the client
              request = Integer.parseInt(inputFromClient.readLine());
              // Process request
              switch(request) {
                  case SEND_LOGIN: {
                      try {
                          handle = inputFromClient.readLine();
                          String password = inputFromClient.readLine();
                          String requested  = inputFromClient.readLine();
                          forum = MysqlQueryBattery.pullForumByForumName(requested);
                          if (forum.isEmpty()) {
                              this.logged_in = connection_code.FORUM_FAIL;
                              textArea.appendText(handle + " denied connecting to forum: " + requested + ", BAD FORUM\n");
                              break;
                          }
                          this.transcript = TranscriptMap.getTranscript(forum.get("forum_name"));

                          String msg = " successfully connecting to " + forum.get("forum_name") + "!\n";
                          boolean success = MysqlQueryBattery.tryLogin(handle, password);
                          if(success) {
                              this.logged_in = connection_code.SUCCESS;
                          }
                          else {
                              this.logged_in = connection_code.CRED_FAIL;
                          }
                          Platform.runLater( () -> {
                              if (this.logged_in == connection_code.SUCCESS) {
                                  MysqlQueryBattery.pushMessage("Server", "debug", handle + " successfully connecting to forum " + forum.get("forum_name") + "!\n", forum.get("ForumID"));
                                  textArea.appendText("Client #" + this.clientNo + " (" + handle + ") " + msg);
                              } else {
                                  textArea.appendText("Refused connection request to Forum: " + forum.get("forum_name") + " for username: " + handle + ", BAD CREDENTIALS\n");
                              }
                          });
                      }
                      catch (IOException ex) {
                          ex.printStackTrace();
                      }
                      break;
                  }
                  case GET_LOGIN: {
                      switch (this.logged_in) {
                          case SUCCESS:
                              transcript.addComment("Server", "debug", handle + " successfully connecting to forum " + forum.get("forum_name") + "!\n", DateTimeUtils.getCurrentTimeStamp());
                              outputToClient.println("0");
                              break;
                          case FORUM_FAIL:
                              outputToClient.println("-1");
                              break;
                          case CRED_FAIL:
                              outputToClient.println("-2");
                              break;
                      }
                      outputToClient.flush();
                      break;
                  }
                  case SEND_COMMENT: {
                      String comment = inputFromClient.readLine();
                      MysqlQueryBattery.pushMessage(this.handle, "text", comment, forum.get("ForumID"));
                      transcript.addComment(handle, "text", comment, DateTimeUtils.getCurrentTimeStamp());
                      break;
                  }
                  case SEND_FILE: {
                      String file_path = inputFromClient.readLine();
//                      Path path = Paths.get(file_path);
//                      System.out.println(Files.exists(path, LinkOption.NOFOLLOW_LINKS));
                      MysqlQueryBattery.pushMessage(this.handle, "file", file_path, forum.get("ForumID"));
                      transcript.addComment(handle, "file", file_path, DateTimeUtils.getCurrentTimeStamp());
                      break;
                  }
                  case GET_COMMENT_COUNT: {
                      outputToClient.println(transcript.getSize());
                      outputToClient.flush();
                      break;
                  }
                  case GET_COMMENT: {
                      int n = Integer.parseInt(inputFromClient.readLine());
                      outputToClient.println(transcript.getComment(n));
                      outputToClient.flush();
                      break;
                  }
              }
            }
          }
          catch(NumberFormatException ex) { // In the event the client disconnects, the malformed request code is interpreted as client terminated connection
              String disconnectMsg = "Client #" + this.clientNo + " (" + handle + ") has disconnected from forum " + forum.get("forum_name") + "\n";
              MysqlQueryBattery.pushMessage("Server", "debug", handle + " has disconnected\n", forum.get("ForumID"));
              transcript.addComment("Server", "debug", handle + " has disconnected\n", DateTimeUtils.getCurrentTimeStamp());
              Platform.runLater(()->textArea.appendText(disconnectMsg));
          }
          catch(Exception ex) {
              ex.printStackTrace();
              Platform.runLater(()->textArea.appendText("Forum: " + forum.get("forum_name") + ", Exception in client thread: " + ex.toString() + "\n"));
          }
        }
  }