package chatserver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class holds a literal HashMap of the forums tied by their name to the transcript instance of that forum.
 */
public class TranscriptMap {

    private static HashMap<String, Transcript> transcriptHashMap;   // map of the transcripts that are in the database, on startup of the server.

    public TranscriptMap() {
        transcriptHashMap = new HashMap<String, Transcript>();

        ArrayList forums = MysqlQueryBattery.pullAllForums();   // query for all forums
        for (Object f : forums) {
            HashMap forum = (HashMap) f;
            String forumID = (String) forum.get("ForumID");
            String forumName = (String) forum.get("forum_name");

            ArrayList messages = MysqlQueryBattery.pullMessagesByForumID(forumID);  // query for all messages of forum 'df'

            Collections.sort(messages, new Comparator<Map<String, String>>() {  // sort the messages of the result by date, the defacto 'order by date desc' failed so this was the only way to make them ordered.
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                @Override
                public int compare(final Map<String, String> map1, final Map<String, String> map2) {
                    String key1 = map1.get("date_posted");
                    String key2 = map2.get("date_posted");
                    try {
                        return df.parse(key1).compareTo(df.parse(key2));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });

            Transcript transcript = new Transcript();   // instantiate transcript and, next, populate it
            for (Object m : messages) {
                HashMap message = (HashMap) m;
                String own = (String) message.get("own");
                String type = (String) message.get("message_type");
                String body = (String) message.get("message_body");
                String date_time = (String) message.get("date_posted");
                transcript.addComment(own, type, body, date_time);
            }
            transcriptHashMap.put(forumName, transcript);   // finally add to map of all forums by name of forum.
        }
    }

    /** return the requested Transcript by given key, 'forumName' */
    public static Transcript getTranscript(String forumName) {
        return transcriptHashMap.get(forumName);
    }

    public static HashMap<String, Transcript> getTranscriptHashMap() {
        return transcriptHashMap;
    }

    public static void printTranscriptHashMap() {
        Iterator it1 = transcriptHashMap.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry pair1 = (Map.Entry)it1.next();
            System.out.println(pair1.getKey() + " : " + pair1.getValue().toString());
//            HashMap temp = (HashMap) pair1.getValue();
//            Iterator it2 = temp.entrySet().iterator();
//            while (it2.hasNext()) {
//                Map.Entry pair2 = (Map.Entry)it2.next();
//                System.out.println("\t" + pair2.getKey() + " = " + pair2.getValue());
//                it2.remove(); // avoids a ConcurrentModificationException
//            }
            it1.remove();
        }
    }
}
