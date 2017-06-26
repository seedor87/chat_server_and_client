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
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

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
    private Transcript transcript;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transcript = new Transcript();
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
          new Thread(new HandleAClient(socket,transcript,textArea, clientNo)).start();
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
    private boolean logged_in;

    public HandleAClient(Socket socket,Transcript transcript,TextArea textArea, int clientNo) {

        this.socket = socket;
        this.transcript = transcript;
        this.textArea = textArea;
        this.clientNo = clientNo;
        this.logged_in = false;
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
                      forum = MysqlQueryBattery.pullForumByForumName(inputFromClient.readLine());
                      String msg = " successfully connecting to " + forum.get("forum_name") + "!\n";
                      this.logged_in = MysqlQueryBattery.tryLogin(handle, password);
                      Platform.runLater( () -> {
                          if (this.logged_in) {
                              textArea.appendText("Client # " + this.clientNo + " (" + handle + ") " + msg);
                          } else {
                              textArea.appendText("refused connection request to username: " + handle + "\n");
                          }
                      });
                  }
                  catch (IOException ex) {
                      ex.printStackTrace();
                  }
                  break;
              }
              case GET_HANDLE: {
                  if (this.logged_in) {
                      transcript.addComment(handle + " successfully connecting to " + forum.get("forum_name") + "!\n");
                      outputToClient.println("0");
                  }
                  else {
                      outputToClient.println("-1");
                  }
                  outputToClient.flush();
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
          String disconnectMsg = "Client #" + this.clientNo + " (" + handle + ") has disconnected\n";
          transcript.addComment(disconnectMsg);
          Platform.runLater(()->textArea.appendText(disconnectMsg));
      }
      catch(Exception ex) {
          ex.printStackTrace();
          Platform.runLater(()->textArea.appendText("Exception in client thread: " + ex.toString() + "\n"));
      }
    }
  }