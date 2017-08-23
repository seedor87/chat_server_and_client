package chatserver;

import chatserver.DateTimeUtils;
import chatserver.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Transcript {

    private List<Message> transcript = Collections.synchronizedList(new ArrayList<Message>());
    
    public Transcript() {
        transcript.add(0, new Message("", "debug", "Beginning Threaded Transcript", DateTimeUtils.getCurrentTimeStamp()));
    }
    
    public void addComment(String own, String type, String body, String date_time) { transcript.add(new Message(own, type, body, date_time)); }

    public int getSize() { return transcript.size(); }

    public String getComment(int n) { return transcript.get(n).toString(); }

    public String toString() {
        String ret = "";
        for (Message comment: transcript) {
            ret += comment.toString() + "\n";
        }
        return ret;
    }
}
