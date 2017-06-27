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
import java.util.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 *
 * @author Joe Gregg
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private TextArea textArea;
    
    private int clientNo = 0;
    private TranscriptMap transcriptMap;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        transcriptMap = new TranscriptMap(); // Init transcriptMap to hold the state of each forum's transcript as it was left

        this.textArea.setEditable(false);

        Platform.runLater( () -> {
            // Display the client number
            textArea.appendText("Server Started at: " + new Date() + '\n');
        });

        new Thread( () -> {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
        
        while (true) {
          // Listen for a new connection request
          Socket socket = serverSocket.accept();
    
          // Increment clientNo
          clientNo++;
          
          Platform.runLater( () -> {
            // Display the client number
            textArea.appendText("Starting thread for Client #" + clientNo + " at " + new Date() + '\n');
            });
          
          // Create and start a new thread for the connection
          new Thread(new HandleAClient(socket,textArea, clientNo)).start();
        }
      }
      catch(IOException ex) {
        ex.printStackTrace();
      }
    }).start();
    }
}

class HandleAClient implements Runnable, chat.ChatConstants {

    private Socket socket; // A connected socket
    private Transcript transcript; // Reference to shared transcript
    private TextArea textArea;
    private String handle;
    private HashMap<String, String> forum;
    private int clientNo;

    private enum connection_code {
        CRED_FAIL, FORUM_FAIL, SUCCESS
    }
    private connection_code logged_in;

    public HandleAClient(Socket socket, TextArea textArea, int clientNo) {

        this.socket = socket;
        this.textArea = textArea;
        this.clientNo = clientNo;
        this.logged_in = null;
        this.transcript = null;
    }

    public void run() {
      try {
        // Create reading and writing streams
        BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());

        // Continuously serve the client
        while (true) {
          // Receive request code from the client
          int request = Integer.parseInt(inputFromClient.readLine());
          // Process request
          switch(request) {
              case SEND_HANDLE: {
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
              case GET_HANDLE: {
                  switch (this.logged_in) {
                      case SUCCESS:
                          transcript.addComment(handle + " successfully connecting to forum " + forum.get("forum_name") + "!\n");
                          outputToClient.println("0");
                          outputToClient.flush();
                          break;
                      case FORUM_FAIL:
                          outputToClient.println("-1");
                          outputToClient.flush();
                          break;
                      case CRED_FAIL:
                          outputToClient.println("-2");
                          outputToClient.flush();
                          break;
                  }
                  break;
              }
              case SEND_COMMENT: {
                  String comment = inputFromClient.readLine();
                  MysqlQueryBattery.pushMessage(this.handle, "text", comment, forum.get("ForumID"));
                  transcript.addComment(handle + "> " + comment);
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
      catch(NumberFormatException ex) {
          String disconnectMsg = "Client #" + this.clientNo + " (" + handle + ") has disconnected from forum " + forum.get("forum_name") + "\n";
          transcript.addComment(handle + " has disconnected\n");
          Platform.runLater(()->textArea.appendText(disconnectMsg));
      }
      catch(Exception ex) {
          ex.printStackTrace();
          Platform.runLater(()->textArea.appendText("Forum: " + forum.get("forum_name") + ", Exception in client thread: " + ex.toString() + "\n"));
      }
    }
  }