/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 *
 * @author Joe Gregg
 */
public class FXMLDocumentController implements Initializable {
    private ChatGateway gateway;

    @FXML
    private TextArea textArea;
    @FXML
    private TextField comment;
    @FXML
    private Button send;
           
    
    @FXML
    private void sendComment(ActionEvent event) {
        String text = comment.getText();
        gateway.sendComment(text);
        comment.clear();
    }

    @FXML
    private void pushedButton(ActionEvent event) {
        gateway.sendComment("¯\\_(ツ)_/¯");
        comment.clear();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.textArea.setEditable(false);
        this.send.setDefaultButton(true);
        gateway = new ChatGateway(textArea);

        if (!gateway.ping()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Server Error");
            alert.setHeaderText("Internal Server Error");
            alert.setContentText("The server cannot be reached now please try again, or contact admin");
            alert.showAndWait();
            System.exit(0);
        }

        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Login");

//        dialog.setOnCloseRequest(
//                event -> System.exit(0)
//        );

//        // Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        TextField forum = new TextField();
        forum.setPromptText("Forum");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Forum:"), 0, 2);
        grid.add(forum, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            if (dialogButton == cancelButtonType) {
                System.exit(0);
            }
            return null;
        });

        boolean succesfulLogin = false;
        do {
            Optional<Pair<String, String>> res = dialog.showAndWait();

            res.ifPresent(usernamePassword -> {
                gateway.sendHandle(usernamePassword.getKey(), usernamePassword.getValue(), forum.getText());
            });
            int result = gateway.getHandle();
            String message = "";
            switch(result) {
                case -2:
                    message = "Wrong username or password. Try again.";
                    break;
                case -1:
                    message = "Invalid forum: '" + forum.getText() + "'. Try again.";
                    break;
                case 0:
                    succesfulLogin = true;
                    break;
            }
            if (!succesfulLogin) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText("There Was A Problem Logging In");
                alert.setContentText(message);
                alert.showAndWait();
            }
        }
        while(!succesfulLogin);

        ChatClient.setTitle(username.getText() + "'s Session");

        // Start the transcript check thread
        new Thread(new TranscriptCheck(gateway,textArea)).start();
    }
}

class TranscriptCheck implements Runnable, chat.ChatConstants {
    private ChatGateway gateway; // Gateway to the server
    private TextArea textArea; // Where to display comments
    private int N; // How many comments we have read
    
    /** Construct a thread */
    public TranscriptCheck(ChatGateway gateway,TextArea textArea) {
      this.gateway = gateway;
      this.textArea = textArea;
      this.N = 0;
    }

    /** Run a thread */
    public void run() {

        while(true) {
          if (gateway.getCommentCount() > N) {
              String newComment = gateway.getComment(N);
              Platform.runLater(() -> textArea.appendText(newComment + "\n"));
              N++;
          } else {
              try {
                  Thread.sleep(250);
              }
              catch (InterruptedException ex) {}
          }
        }
    }
  }