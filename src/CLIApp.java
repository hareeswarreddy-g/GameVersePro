public class CLIApp {
    public static void main(String[] args) {
        try {
            // ensure schema + seed data
            try (var c = DBConnection.getConnection()) {
                c.setAutoCommit(false);

                // create tables if not exist
                var stmt = c.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE, wallet DECIMAL(10,2) DEFAULT 0.00)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS games (game_id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL, price DECIMAL(8,2) NOT NULL, stock INT DEFAULT 0)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS purchases (purchase_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, game_id INT NOT NULL, amount DECIMAL(8,2) NOT NULL, status VARCHAR(50) NOT NULL, purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                // create sample user if absent
                var psu = c.prepareStatement("SELECT user_id FROM users WHERE username=?");
                psu.setString(1, "alice");
                var rsu = psu.executeQuery();
                int userId;
                if (rsu.next()) {
                    userId = rsu.getInt(1);
                } else {
                    var ins = c.prepareStatement("INSERT INTO users(username,wallet) VALUES(?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);
                    ins.setString(1, "alice"); ins.setDouble(2, 100.0); ins.executeUpdate();
                    var k = ins.getGeneratedKeys(); k.next(); userId = k.getInt(1);
                }

                // create sample game if absent
                var psg = c.prepareStatement("SELECT game_id FROM games WHERE title=?");
                psg.setString(1, "Example Game");
                var rsg = psg.executeQuery();
                int gameId;
                if (rsg.next()) {
                    gameId = rsg.getInt(1);
                } else {
                    var insg = c.prepareStatement("INSERT INTO games(title,price,stock) VALUES(?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);
                    insg.setString(1, "Example Game"); insg.setDouble(2, 19.99); insg.setInt(3, 10); insg.executeUpdate();
                    var k2 = insg.getGeneratedKeys(); k2.next(); gameId = k2.getInt(1);
                }

                c.commit();
                System.out.println("Seeded user id="+userId+" and game id="+gameId);

                // attempt purchase
                PurchaseService svc = new PurchaseService();
                try {
                    boolean ok = svc.buyGame(userId, gameId);
                    System.out.println("Purchase result: "+ok);
                } catch (Exception ex) {
                    System.out.println("Purchase failed: "+ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
