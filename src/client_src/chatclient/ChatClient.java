package chatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This file is the class that stores the instance of the client session created on startup.
 */
public class ChatClient extends Application {

    private static String title = "Temp Title";
    private static Stage clientStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        /**
         * The main method that runs on startup.
         * clientStage inherits the default JavaFX stage from the parent, Application class.
         */
        clientStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("ClientFXMLDocument.fxml")); // load the associated fxml file to render chat session.
        
        Scene scene = new Scene(root);

        clientStage.setScene(scene);
        clientStage.setTitle(title);
        clientStage.setOnCloseRequest(event->System.exit(0));
        clientStage.show();
    }

    public static void setTitle(String newTitle) {
        title = newTitle;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
