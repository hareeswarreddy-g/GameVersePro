import java.sql.Connection;
import java.sql.PreparedStatement;

public class Restock {
    public static void main(String[] args) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE games SET stock = stock + 25")) {
            int updated = ps.executeUpdate();
            System.out.println("Restocked " + updated + " games. Each game now has +25 stock.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
