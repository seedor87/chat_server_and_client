package chatclient;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


/**
 * The message manufacturer makes arrayLists of JavaFX Text instances that are loaded into the textFlow of the UI.
 * These are dynamically created per the message type.
 * Only method 'Manufacture' is used from outside this file.
 */
public class MessageManufacturer {

    private static final String IMG_PATH = ".\\ASRCFH.png";

    final static Clipboard clipboard = Clipboard.getSystemClipboard();
    final static ClipboardContent content = new ClipboardContent();

    /**
     * Special event handler for Text that copies to system clipboard on right-click.
     */
    private static EventHandler copy = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent e) {
            Text source = (Text) e.getSource();
            if (e.getButton().equals(MouseButton.SECONDARY) && e.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                source.setUnderline(true);
                content.putString(source.getText());
                clipboard.setContent(content);
            }
            else {
                source.setUnderline(false);
            }
        }
    };

    /**
     * Custom event handler that simulates hyperlink to allow special action on click.
     * In this case we open a file chooser dialogue to the location of the file's immediate parent directory over the current stage.
     */
    private static EventHandler activateElse = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent e) {
            Text source = (Text) e.getSource();
            source.setFill(Color.MEDIUMPURPLE);
            File initDir = new File(new File(source.getText()).getParentFile().getAbsolutePath());
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(initDir);
            fc.showOpenDialog(new Stage());
        }
    };

    /**
     * Custom event handler for action specific to the extension .jnote, for our own java annotate files
     */
    private static EventHandler activateJnote = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent e) {
            Text source = (Text) e.getSource();
            source.setFill(Color.MEDIUMPURPLE);

            if(System.getProperty("os.name").equals("Linux")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Openeing Jnote File");
                alert.setHeaderText("You Have Chosen a File With The Extension .jnote");
                alert.setContentText("Now Opening Java Annotation Program.\nProceed?");

                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK) {
                    String[] annotationArgs = new String[]{"xdg-open", source.getText().trim()};
                    for (String arg : annotationArgs) {
                        System.out.print(arg + " ");
                    }
                    try {
                        Process proc = new ProcessBuilder(annotationArgs).start();
                    } catch (IOException ioe) {
                        System.out.println("test");
                        ioe.printStackTrace();
                    }
                } else {
                    return;
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invlaid Configuration");
                alert.setContentText("Cannot Open .jnote File From Here, try using Linux Instead");
                alert.showAndWait();
                return;
            }
        }
    };

    /**
     * Produce array of Texts using params.
     *
     * @param ownerString - string for message owner username.
     * @param dateString - string for date messages posted (server determined)
     * @param typeString - string for type of message (only text / comment and file supported)
     * @param bodyString - String for body of message (text-type) or path to file (file-type)
     * @return ArrayList of Texts for UI's textFlow to render.
     */
    public static ArrayList<Node> Manufacture(String ownerString, String dateString, String typeString, String bodyString) {

        ArrayList<Node> ret = new ArrayList<>();

        final Text ownerText = new Text(ownerString);
        final Text dateText = new Text(dateString);
        final Text typeText = new Text(typeString);
        final Text bodyText = new Text(bodyString);
        ownerText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        if (ownerString.contains("Server")) {
            ownerText.setFill(Color.MEDIUMVIOLETRED);
        } else {
            ownerText.setFill(Color.CADETBLUE);
        }
        dateText.setFont(Font.font("Verdana", FontPosture.ITALIC, 14));
        dateText.setFill(Color.DARKGOLDENROD);
        typeText.setFont(Font.font("Verdana", FontPosture.ITALIC, 14));
        bodyText.setFont(Font.font("Verdana", FontPosture.REGULAR, 16));
        bodyText.setFont(Font.font("Verdana", FontPosture.REGULAR, 16));

        /**
         * NOTE:
         * If you construct too much content in one method call, the threads cannot keep up and the count (n) of the number of messages will be off.
         * This means, for now, we are forced to hold off on adding too many objects (see below) to the client text floe.
         *
         * final Separator separator = new Separator(Orientation.HORIZONTAL);
         * separator.prefWidthProperty().bind(textFlow.widthProperty());
         * separator.setStyle("-fx-background-color: red;");
        */

        ownerText.addEventHandler(MouseEvent.ANY, copy);
        dateText.addEventHandler(MouseEvent.ANY, copy);
        typeText.addEventHandler(MouseEvent.ANY, copy);
        bodyText.addEventHandler(MouseEvent.ANY, copy);

        ret.add(ownerText);
        ret.add(new Text(new String(new char[50 - ownerText.getText().length()]).replace('\0', ' ')));
        ret.add(dateText);
        ret.add(new Text("\n"));
        ret.add(typeText);
        ret.add(new Text(new String(new char[25 - ownerText.getText().length()]).replace('\0', ' ')));

        if (typeString.contains("file")) {
            if(bodyString.contains(".jnote")) {
                ImageView imview = new ImageView(new Image(IMG_PATH));
                ret.add(new Text("\n"));
                ret.add(imview);
                ret.add(new Text("\n"));

                bodyText.addEventHandler(MouseEvent.MOUSE_CLICKED, activateJnote);
            } else {
                bodyText.addEventHandler(MouseEvent.MOUSE_CLICKED, activateElse);
            }
            bodyText.setFill(Color.DARKBLUE);
        }
        ret.add(bodyText);

        ret.add(new Text("\n"));
        ret.add(new Text(System.lineSeparator()));
        ret.add(new Text(System.lineSeparator()));
        return ret;
    }
}
