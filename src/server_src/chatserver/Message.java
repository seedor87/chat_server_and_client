package chatserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class encapsulates the paradigm of the message object.
 * Each instances holds any information important about the message from (or into) the db.
 */
public class Message {

    private String body;        // body of comment (file path for file type comment)
    private String own;         // who posted the comment
    private String date_time;   // when it was posted (see sql schema for details)
    private String type;        // the type of message (only 'text' and 'file' are supoorted now)
    private JSONObject message = new JSONObject();  // the json object that is pumped over the wire to the client for unpacking.

    /** construct a message instance */
    public Message(String own, String type, String body, String date_time) {
        this.own = own;
        this.type = type;
        this.body = body;
        this.date_time = date_time;

        try {
            message.put("own", own);
            message.put("type", type);
            message.put("body", body);
            message.put("date_time", date_time);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public String getBody() { return this.body; }
    public String getOwn() { return this.own; }
    public String getDate_time() { return this.date_time; }
    public String getType() { return this.type; }
    public JSONObject getMessage() { return this.message; }

    /**
     * return the JSON object of this instance using java's JSON
     * This method is used when the instance needs to passed to an unattached client who can parse the args as needed.
     *
     * @return - String object of the field 'message' (with \n's replaced) for buffered output passing.
     * (for usage see Transcript.java)
     */
    public String toString() {
        try {
            // replace all newlines with empty to conform to single line comms with any client
            return this.message.toString(2).replace("\n", "");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}