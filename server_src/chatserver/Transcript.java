package chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Transcript {

    private class Message {

        private String body;

        public Message(String body) { this.body = body; }

        public String getBody() { return this.body; }
    }

    private List<Message> transcript = Collections.synchronizedList(new ArrayList<Message>());
    
    public Transcript() {
        transcript.add(0, new Message("Beginning Threaded Transcript"));
    }
    
    public void addComment(String comment) { transcript.add(new Message(comment)); }

    public int getSize() { return transcript.size(); }

    public String getComment(int n) { return transcript.get(n).getBody(); }
}
