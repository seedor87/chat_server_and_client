package chatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Joe Gregg
 */
public class ChatClient extends Application {

    private static String title = "Temp Title";
    private static Stage clientStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        clientStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
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
