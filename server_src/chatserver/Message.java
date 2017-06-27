package chatserver;

/**
 * Created by robertseedorf on 6/26/17.
 */
public class Message {

    private String body;

    public Message(String body) { this.body = body; }

    public String getBody() { return this.body; }

    public String toString() {
        return body.toString();
    }
}