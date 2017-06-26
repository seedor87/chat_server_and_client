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
        gateway = new ChatGateway(textArea);

//        // Put up a dialog to get a handle from the user
//        TextInputDialog user_dialog = new TextInputDialog();
//        user_dialog.setTitle("Choose A Handle");
//        user_dialog.setHeaderText(null);
//        user_dialog.setContentText("Enter a handle:");
//
//        Optional<String> result = user_dialog.showAndWait();
//        result.ifPresent(name -> gateway.sendHandle(name));
//
//        TextInputDialog target_dialog = new TextInputDialog();
//        target_dialog.setTitle("Select A Forum");
//        target_dialog.setHeaderText(null);
//        target_dialog.setContentText("Enter a target forum:");
//
//        Optional<String> forum = target_dialog.showAndWait();
//        forum.ifPresent(target -> gateway.sendForum(target));


        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Login");

//        // Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

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
            return null;
        });

        boolean succesfulLogin = false;
        do {
            Optional<Pair<String, String>> res = dialog.showAndWait();

            res.ifPresent(usernamePassword -> {
                gateway.sendHandle(usernamePassword.getKey(), usernamePassword.getValue(), forum.getText());
            });
            succesfulLogin = gateway.getHandle();
        }
        while(!succesfulLogin);

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