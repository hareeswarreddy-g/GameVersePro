import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User findByUsername(String username) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT user_id,username,wallet FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("username"), rs.getDouble("wallet"));
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT user_id,username,wallet FROM users WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new User(rs.getInt("user_id"), rs.getString("username"), rs.getDouble("wallet"));
            }
        }
        return null;
    }

    public User create(String username, double initialWallet) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,wallet) VALUES(?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setDouble(2, initialWallet);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return new User(keys.getInt(1), username, initialWallet);
            }
        }
        return null;
    }

    public void updateWalletDelta(Connection c, int userId, double delta) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET wallet=wallet+? WHERE user_id=?")) {
            ps.setDouble(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
