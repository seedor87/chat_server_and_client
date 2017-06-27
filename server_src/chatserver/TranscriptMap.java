package chatserver;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by robertseedorf on 6/27/17.
 */
public class TranscriptMap {

    private static HashMap<String, Transcript> transcriptHashMap;

    public TranscriptMap() {
        transcriptHashMap = new HashMap<String, Transcript>();

        ArrayList forums = MysqlQueryBattery.pullAllForums();
        for (Object f : forums) {
            HashMap forum = (HashMap) f;
            String forumID = (String) forum.get("ForumID");
            String forumName = (String) forum.get("forum_name");

            ArrayList messages = MysqlQueryBattery.pullMessagesByForumID((String) forum.get("ForumID"));

            Collections.sort(messages, new Comparator<Map<String, String>>() {
                DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                @Override
                public int compare(final Map<String, String> map1, final Map<String, String> map2) {
                    String key1 = map1.get("date_posted");
                    String key2 = map2.get("date_posted");
                    try {
                        return f.parse(key1).compareTo(f.parse(key2));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });

            Transcript transcript = new Transcript();
            for (Object m : messages) {
                HashMap message = (HashMap) m;
                String own = (String) message.get("own");
                String comment = (String) message.get("message_body");
                transcript.addComment(own + "> " + comment);
            }
            transcriptHashMap.put(forumName, transcript);
        }
    }

    public static Transcript getTranscript(String key) {
        return transcriptHashMap.get(key);
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
