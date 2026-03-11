import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public void insert(Connection c, int userId, int gameId, double amount, String status) throws Exception {
        try (PreparedStatement ps = c
                .prepareStatement("INSERT INTO purchases(user_id,game_id,amount,status) VALUES(?,?,?,?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, gameId);
            ps.setDouble(3, amount);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    public List<Purchase> getPurchasesByUser(int userId) throws Exception {
        List<Purchase> purchases = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT purchase_id, user_id, game_id, amount, status, purchased_at FROM purchases WHERE user_id = ? AND status = 'SUCCESS'")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Purchase p = new Purchase();
                    p.setPurchaseId(rs.getInt("purchase_id"));
                    p.setUserId(rs.getInt("user_id"));
                    p.setGameId(rs.getInt("game_id"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setStatus(rs.getString("status"));
                    p.setPurchasedAt(rs.getTimestamp("purchased_at"));
                    purchases.add(p);
                }
            }
        }return purchases;
}}
