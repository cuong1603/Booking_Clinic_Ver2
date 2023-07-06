
package context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class DBContext {

    private final String condition = "allowPublicKeyRetrieval=true&verifyServerCertificate=false&useSSL=false&requireSSL=false";

    public Connection getConnection() {
        try {
            // Edit URL , username, password to authenticate with your MS SQL Server
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverName = "localhost";
            String portNumber = "3306";
            String dbName = "doctris_system";
            String url = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName + "?" + condition;

            String username = "root";
            String password = "123456";

            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex);
        }
        return null;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        DBContext db = new DBContext();
        System.out.println(db.getConnection());
    }
}
