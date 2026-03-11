import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    public List<Game> listAll() throws Exception {
        List<Game> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT game_id,title,price,stock FROM games")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Game(rs.getInt("game_id"), rs.getString("title"), rs.getDouble("price"), rs.getInt("stock")));
            }
        }
        return out;
    }

    public Game findById(int gameId) throws Exception {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT game_id,title,price,stock FROM games WHERE game_id=?")) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Game(rs.getInt("game_id"), rs.getString("title"), rs.getDouble("price"), rs.getInt("stock"));
            }
        }
        return null;
    }

    public void decrementStock(Connection c, int gameId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("UPDATE games SET stock=stock-1 WHERE game_id=? AND stock>0")) {
            ps.setInt(1, gameId);
            ps.executeUpdate();
        }
    }
}
