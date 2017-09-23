package chatserver;

import javafx.scene.control.Alert;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class holds the prefabricated queries that are used for database interaction.
 */
public class MysqlQueryBattery {

    private static final DataSource DATA_SOURCE = ConnectionManager.getMySQLDataSource();
    private static final String DB_TABLE = ConnectionManager.getDB();

    /** the following are the key lists that are used for programmatic iteration over the results sets for simplified operations*/
    private static final ArrayList<String> message_keys = new ArrayList<String>(Arrays.asList("own", "message_type", "message_body", "date_posted"));
    private static final ArrayList<String> user_keys = new ArrayList<String>(Arrays.asList("Username", "email", "pass", "role"));
    private static final ArrayList<String> forum_keys = new ArrayList<String>(Arrays.asList("ForumID", "forum_name"));

    private static ResultSet result_set = null;
    private static Connection connection = null;
    private static Statement statement = null;

    /** Used every time a query is tried to make sure that the connection is established */
    public static boolean verifyConnection() {
        try {
//            String url = "jdbc:mysql://165.227.208.204:3306/java_chat";
//            String user = "bob";
//            String password = "password";
//            Class.forName("com.mysql.jdbc.Driver").newInstance();
//            connection = DriverManager.getConnection(url, user, password);

            connection = DATA_SOURCE.getConnection();

            statement = connection.createStatement();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("Database Connection Error");
            alert.setContentText("There has been an irrecoverable error connecting to the database." +
                    "\nContact Admin or try again later");
            alert.showAndWait();
            System.exit(1);
        }
        return true;
    }

    /** returns true only when the login is a success */
    public static boolean tryLogin(String Username, String password) {
        String pass = null;
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Users where Username = '%s'", DB_TABLE, Username);
                result_set = statement.executeQuery(query);

                if (result_set.next()) {
                    pass = result_set.getString("pass");
                    return pass.equals(password);   // if stored password exactly matches the one given by user; return true
                }
                else {
                    System.out.print("NO RESULTS");
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * This method queries all the ascribed forums and stores the resulting table in an ArrayList of HashMaps.
     *
     * @return - ArrayList all forums in the db.
     */
    public static ArrayList pullAllForums() {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Forums", DB_TABLE);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : forum_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static HashMap<String, String> pullUser(String Username) {
        HashMap ret = new HashMap<String, String>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Users where Username = '%s'", DB_TABLE, Username);
                result_set = statement.executeQuery(query);

                if (result_set.next()) {
                    for (String key : user_keys) {
                        ret.put(key, result_set.getString(key));
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static HashMap<String, String> pullForumByID(String ForumID) {
        HashMap ret = new HashMap<String, String>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Forums where ForumID = '%s'", DB_TABLE, ForumID);
                result_set = statement.executeQuery(query);

                if (result_set.next()) {
                    for (String key : forum_keys) {
                        ret.put(key, result_set.getString(key));
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * This method queries all the ascribed forums for the one named by the param and stores the resulting table in an ArrayList of HashMaps.
     *
     * @return - HashMap the forum keys (table column) tied to their values
     */
    public static HashMap<String, String> pullForumByForumName(String forum_name) {
        HashMap ret = new HashMap<String, String>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Forums where forum_name = '%s'", DB_TABLE, forum_name);
                result_set = statement.executeQuery(query);

                if (result_set.next()) {
                    for (String key : forum_keys) {
                        ret.put(key, result_set.getString(key));
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }


    public static ArrayList pullUsersByRole(String role) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Users where role = '%s'", DB_TABLE, role);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : user_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * This method queries the ascribed forum (by forum name) for all messages in that forum, then adds them to an ArrayList of HashMaps.
     *
     * @return - ArrayList all messages in the given forum, where entries in the List are HashMaps of the message keys (table column) to their values
     */
    public static ArrayList pullMessagesByForumID(String forum_id) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Messages where forum_id = '%s' order by date_posted desc", DB_TABLE, forum_id);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : message_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static ArrayList pullMessagesByUsername(String Username, String forum_id) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Messages where forum_id = '%s' and own = '%s' order by date_posted desc", DB_TABLE, forum_id, Username);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : message_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static ArrayList pullMessagesByType(String type, String forum_id) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Messages where forum_id = '%s' and message_type = '%s' order by date_posted desc", DB_TABLE, forum_id, type);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : message_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static ArrayList pullMessagesByDate(java.sql.Date startDate, String forum_id) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Messages where forum_id = '%s' and date_posted BETWEEN '%s' AND now()", DB_TABLE, forum_id, startDate);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : message_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static ArrayList pullMembership(String Username) {
        ArrayList ret = new ArrayList<String>();
        if(verifyConnection()) {
            try {
                String query = String.format("select distinct ForumID from %s.Users join %s.Members on %s.Users.Username = %s.Members.Username where %s.Users.Username = '%s'", DB_TABLE, DB_TABLE, DB_TABLE, DB_TABLE, DB_TABLE, Username);
                result_set = statement.executeQuery(query);
                while (result_set.next()) {
                    String forum_id = result_set.getString("ForumID");
                    ret.add(forum_id);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static ArrayList pullMembers(String forum_id) {
        ArrayList ret = new ArrayList<HashMap>();
        if(verifyConnection()) {
            try {
                String query = String.format("select * from %s.Users join %s.Members on %s.Users.Username = %s.Members.Username where %s.Members.ForumID = '%s'", DB_TABLE, DB_TABLE, DB_TABLE, DB_TABLE, DB_TABLE, forum_id);
                System.out.println(query);
                result_set = statement.executeQuery(query);

                while (result_set.next()) {
                    HashMap map = new HashMap();
                    for (String key : user_keys) {
                        map.put(key, result_set.getString(key));
                    }
                    ret.add(map);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /*===============================================================================================================*/

    public static boolean pushUser(String Username, String email, String pass, String role) {
        if(verifyConnection()) {
            try {
                String query = String.format("insert into %s.Users (Username, email, pass, role) values (?, ?, ?, ?)", DB_TABLE);

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, Username);
                preparedStmt.setString(2, email);
                preparedStmt.setString(3, pass);
                preparedStmt.setString(4, role);
                preparedStmt.execute();
                return true;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean pushForum(String name) {
        if(verifyConnection()) {
            try {
                String query = String.format("insert into %s.Forums values(UUID(), ?)", DB_TABLE, name);

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, name);
                preparedStmt.execute();
                return true;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean pushMember(String username, String id) {
        if(verifyConnection()) {
            try {
                String query = String.format("insert into %s.Members values(?, ?)", DB_TABLE);

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, username);
                preparedStmt.setString(2, id);
                preparedStmt.execute();
                return true;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Inserts new user into the db
     *
     * @param Username
     * @param type
     * @param message_body
     * @param forum_id
     * @return true if successful query, else false
     */
    public static boolean pushMessage(String Username, String type, String message_body, String forum_id) {
        if(verifyConnection()) {
            try {
                String query = String.format("insert into %s.Messages values(UUID(), ?, ?, ?, now(), ?)", DB_TABLE);

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, Username);
                preparedStmt.setString(2, type);
                preparedStmt.setString(3, message_body);
                preparedStmt.setString(4, forum_id);
                preparedStmt.execute();
                return true;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void main(String args[]) {
//        print(tryLogin("rjseedorf", "password"));
//
//        print(pullMessagesByForumID("f168f344-5051-11e7-b552-0050569c1cce"));
//        print(pullMessagesByUsername("kakoue66", "f168f344-5051-11e7-b552-0050569c1cce"));
//        print(pullMessagesByType("text", "f168f344-5051-11e7-b552-0050569c1cce"));
//
//        Calendar calendar = Calendar.getInstance();
//        java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());
//
//        print(pullMessagesByDate(startDate, "f168f344-5051-11e7-b552-0050569c1cce"));
//
//        print(pullMembership("brownb8"));
//        print(pullMembers("f168f344-5051-11e7-b552-0050569c1cce"));
//
//        print(pullUsersByRole("Admin"));
//        print(pullUser("kakoue66"));
//        print(pullForumByID("7d2c714b-5055-11e7-b552-0050569c1cce"));

        /*
        The following are the steps in order that are run when a new member is created,
        they enter a new forum with a specific member,
        a new message is sent in that forum,
        and finally the forum is updated with the new message.
         */

//        print(pushUser("bob", "bob@bob.com", "pass", "RI"));
//        print(pushForum("rjseedorf-bob"));
//        String id = pullForumByForumName("rjseedorf-bob").get("ForumID");
//        print(id);

//        print(pushMember("bob", id));
//        print(pushMember("rjseedorf", id));

//        print(pushMessage("bob", "text", "Hey Me!", id));

//        print(pullMessagesByForumID(id));
    }
}
