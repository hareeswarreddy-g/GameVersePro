import java.sql.Connection;

public class TestDb {
    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection()) {
            if (c != null && !c.isClosed()) {
                System.out.println("DB connection OK: " + c.getMetaData().getURL());
            } else {
                System.out.println("DB connection failed.");
            }
        } catch (Exception e) {
            System.out.println("DB test error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
