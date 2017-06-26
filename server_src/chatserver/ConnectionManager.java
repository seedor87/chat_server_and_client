package chatserver;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;

public class ConnectionManager {

    private static String table = "seedor87";

    public static DataSource getMySQLDataSource() {
        MysqlDataSource mysqlDS = new MysqlDataSource();
        mysqlDS.setServerName("elvis.rowan.edu");
        mysqlDS.setUser("seedor87");
        mysqlDS.setPassword("penguin");
        return mysqlDS;
    }

    public static String getDBTable() {
        return table;
    }
}