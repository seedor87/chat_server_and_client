/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.io.File;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class responds to user interaction and governs the client session UI in accordance with response codes sent from the server.
 */
public class ClientFXMLDocumentController implements Initializable {

    private chatclient.ChatGateway gateway;
    private FileChooser fileChooser;

    private static BorderPane bp;
    private static FileChooser fc;

    @FXML
    private BorderPane borderPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextFlow textFlow;
    @FXML
    private TextField textComment;
    @FXML
    private Button sendComment;
    @FXML
    private Button sendFile;
           
    
    @FXML
    private void sendComment(ActionEvent event) {
        String text = textComment.getText();
        gateway.sendComment(text);
        textComment.clear();
    }

    @FXML
    private void sendFile(ActionEvent event) {
        File file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
        if (file != null && file.exists()) {
            gateway.sendFile(file.getAbsolutePath());
            textComment.clear();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        textFlow.setEditable(false);
        this.sendComment.setDefaultButton(true);
        bp = borderPane;
        fc = fileChooser;
        gateway = new ChatGateway(textFlow, scrollPane);

        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJnote = new FileChooser.ExtensionFilter("JNOTE files (*.jnote)", "*.zip");
        FileChooser.ExtensionFilter extFilterZip = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        FileChooser.ExtensionFilter extFilterDoc = new FileChooser.ExtensionFilter("DOCX files (*.docx)", "*.docx");
        FileChooser.ExtensionFilter extFilterTxt = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilterJnote);
        fileChooser.getExtensionFilters().add(extFilterZip);
        fileChooser.getExtensionFilters().add(extFilterDoc);
        fileChooser.getExtensionFilters().add(extFilterTxt);

        sendComment.disableProperty().bind(
                new BooleanBinding() {
                    { super.bind(textComment.textProperty()); }
                    @Override
                    protected boolean computeValue() {
                        return (textComment.getText().isEmpty());
                    }
                }
        );

        if (!gateway.sendPing()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Server Error");
            alert.setHeaderText("Internal Server Error");
            alert.setContentText("The server cannot be reached now please try again, or contact admin");
            alert.showAndWait();
            System.exit(0);
        }

        /*
        This monolith is used to create the dialoge that prompts for username and password on startup.
         */
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Login");

//        dialog.setOnCloseRequest(
//                event -> System.exit(0)
//        );

//        // Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

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

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> username.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            if (dialogButton == cancelButtonType) {
                System.exit(0);
            }
            return null;
        });

        /*
        This code runs until the user successfully logs in, or quits
         */
        boolean succesfulLogin = false; // switch for break on successful login
        do {
            Optional<Pair<String, String>> res = dialog.showAndWait();

            res.ifPresent(usernamePassword -> {
                gateway.sendLogin(usernamePassword.getKey(), usernamePassword.getValue(), forum.getText());
            });
            int result = gateway.getLogin();
            String message = "Unknown Error";
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
                // If failed login show dialog with post message of reason for fail.
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText("There Was A Problem Logging In");
                alert.setContentText(message);
                alert.showAndWait();
            }
        }
        while(!succesfulLogin);

        ChatClient.setTitle(username.getText() + "'s Session");

        new Thread(new TranscriptCheck(gateway, textFlow, scrollPane)).start();
    }
}

/**
 * This class posts to the endless textFlow of the client UI the messages from the server one by one.
 * The way they are posted depends on the message type, there are two different cases right now.
 * The server side interface is in file HandleClient.java, for more info.
 */
class TranscriptCheck implements Runnable, ChatConstants {
    private final String IMAGE_PATH = "file:/Users/robertseedorf/IdeaProjects/chat_client/src/chatclient/ASRCFH.png";

    private ChatGateway gateway; // Gateway to the server
    private ScrollPane scrollPane;
    private TextFlow textFlow; // Where to display comments
    private int N; // How many comments we have read
    
    /** construct a requesting thread */
    public TranscriptCheck(ChatGateway gateway,TextFlow textArea, ScrollPane scrollPane) {
        this.gateway = gateway;
        this.scrollPane = scrollPane;
        this.textFlow = textArea;
        this.N = 0;
    }

    /** Run a thread */
    public void run() {
        int index = 0;

        while(true) {
            index = gateway.getCommentCount();
            if (index > N) { // while there are messages unknown to the client, request for them from the server.

                JSONObject messageObj = gateway.getComment(N);
                String os = "";
                String ts = "";
                String ds = "";
                String bs = "";
                try {
                    os = messageObj.getString("own");
                    ds = messageObj.getString("date_time");
                    ts = messageObj.getString("type");
                    bs = messageObj.getString("body") + "\n";
                } catch (JSONException ex) {
                    os = ex.toString();
                }

                final String ownerString = os;
                final String typeString = ts;
                final String dateString = ds;
                final String bodyString = bs;
                Platform.runLater(() -> {
                    textFlow.getChildren().addAll(MessageManufacturer.Manufacture(ownerString, dateString, typeString, bodyString));
                });
                scrollPane.setVvalue(scrollPane.getVmax());
                N++;

            } else if (index < 1) { // If the server is gone, for any reason, the value for index is no good so we break and tell user.
                break;
            } else { // Every 1/4 second retry for new posts.
                try {
                    Thread.sleep(250);
                }
                catch (InterruptedException ex) {}
            }
        }
        // end of execution, server dies -> we notify the user and quit.
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Server Error");
            alert.setHeaderText("Internal Server Error");
            alert.setContentText("The server cannot be reached now please try again, or contact admin");
            alert.showAndWait();
            System.exit(0);
        });
    }
  }