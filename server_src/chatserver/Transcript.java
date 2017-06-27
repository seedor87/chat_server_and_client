package chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Transcript {

    private List<Message> transcript = Collections.synchronizedList(new ArrayList<Message>());
    
    public Transcript() {
        transcript.add(0, new Message("Beginning Threaded Transcript"));
    }
    
    public void addComment(String comment) { transcript.add(new Message(comment)); }

    public int getSize() { return transcript.size(); }

    public String getComment(int n) { return transcript.get(n).getBody(); }

    public String toString() {
        String ret = "";
        for (Message comment: transcript) {
            ret += comment.toString() + "\n";
        }
        return ret;
    }
}
