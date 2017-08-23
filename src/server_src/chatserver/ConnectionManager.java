package chatserver;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;

/**
 * This class is used to store the hard-coded values that the database (db) needs to know for connection utilities.
 */
public class ConnectionManager {

    private static final String DB = "seedor87";
    private static final String SERVER = "elvis.rowan.edu";
    private static final String USER = "seedor87";
    private static final String PASSWORD = "penguin";

    /**
     * static method that returns one DataSource instance that encapsulates the connection to the db.
     *
     * @return - DataSource the
     */
    public static DataSource getMySQLDataSource() {
        MysqlDataSource mysqlDS = new MysqlDataSource();
        mysqlDS.setServerName(SERVER);
        mysqlDS.setUser(USER);
        mysqlDS.setPassword(PASSWORD);
        return mysqlDS;
    }

    /**
     * This method is used to get the value for the db that is used while connection is established
     * For our purposes this will always return the name of the db we HAVE to use which is 'seedor87'.
     *
     * @return - String the name of the db that holds the tables we are using
     */
    public static String getDB() {
        return DB;
    }
}