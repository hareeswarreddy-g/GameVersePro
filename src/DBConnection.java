import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/gameverse_db?serverTimezone=UTC&useSSL=false";

    public static Connection getConnection() throws Exception {
        Properties p = new Properties();

        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (Exception ignored) {
            // If properties file isn't found, we'll fall back to defaults
        }

        String url = p.getProperty("jdbc.url", DEFAULT_URL);
        String user = p.getProperty("jdbc.user", "root");
        String pass = p.getProperty("jdbc.password", "");

        // Ensure driver is available
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Let the SQLException surface if driver is missing when connecting
        }

        return DriverManager.getConnection(url, user, pass);
    }
}
