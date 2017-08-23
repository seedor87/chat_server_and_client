package chatserver;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple class that holds tools to get machine dependent time for synchronization of message entries' 'date_posted'
 */
public class DateTimeUtils {

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
