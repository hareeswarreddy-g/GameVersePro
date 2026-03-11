import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PurchaseService {

    private final PurchaseDAO purchaseDAO = new PurchaseDAO();

    public boolean buyGame(int userId, int gameId) throws Exception {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // lock game row
            try (PreparedStatement ps = con.prepareStatement("SELECT price,stock FROM games WHERE game_id=? FOR UPDATE")) {
                ps.setInt(1, gameId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Invalid game id");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    if (stock <= 0) throw new IllegalStateException("Out of stock");

                    // check wallet
                    try (PreparedStatement walletPs = con.prepareStatement("SELECT wallet FROM users WHERE user_id=? FOR UPDATE")) {
                        walletPs.setInt(1, userId);
                        try (ResultSet wrs = walletPs.executeQuery()) {
                            if (!wrs.next()) throw new IllegalArgumentException("User not found");
                            double wallet = wrs.getDouble("wallet");
                            if (wallet < price) throw new IllegalStateException("Insufficient funds");
                        }
                    }

                    // deduct stock
                    try (PreparedStatement upd = con.prepareStatement("UPDATE games SET stock=stock-1 WHERE game_id=?")) {
                        upd.setInt(1, gameId);
                        upd.executeUpdate();
                    }

                    // deduct wallet
                    try (PreparedStatement uw = con.prepareStatement("UPDATE users SET wallet=wallet-? WHERE user_id=?")) {
                        uw.setDouble(1, price);
                        uw.setInt(2, userId);
                        uw.executeUpdate();
                    }

                    // insert purchase
                    purchaseDAO.insert(con, userId, gameId, price, "SUCCESS");

                    con.commit();
                    return true;
                }
            }
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch(Exception ignored){}
            throw e;
        } finally {
            if (con != null) try { con.close(); } catch(Exception ignored){}
        }
    }
}
