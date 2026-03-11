import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Scanner;

public class GameVerseApp {

    static void buyGame(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            @SuppressWarnings("resource")
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter Game ID: ");
            int gameId = sc.nextInt();

            String lockQuery = "SELECT price, stock FROM games WHERE game_id=? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(lockQuery)) {
                ps.setInt(1, gameId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Invalid Game ID.");
                        return;
                    }

                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");

                    if (stock <= 0) {
                        System.out.println("Out of stock.");
                        return;
                    }

                    try (PreparedStatement walletPs = con.prepareStatement("SELECT wallet FROM users WHERE user_id=?")) {
                        walletPs.setInt(1, userId);
                        try (ResultSet walletRs = walletPs.executeQuery()) {
                            if (!walletRs.next()) {
                                System.out.println("User not found.");
                                return;
                            }
                            double wallet = walletRs.getDouble("wallet");
                            if (wallet < price) {
                                System.out.println("Insufficient wallet balance.");
                                return;
                            }
                        }
                    }

                    try (PreparedStatement updateStock = con.prepareStatement("UPDATE games SET stock=stock-1 WHERE game_id=?")) {
                        updateStock.setInt(1, gameId);
                        updateStock.executeUpdate();
                    }

                    try (PreparedStatement updateWallet = con.prepareStatement("UPDATE users SET wallet=wallet-? WHERE user_id=?")) {
                        updateWallet.setDouble(1, price);
                        updateWallet.setInt(2, userId);
                        updateWallet.executeUpdate();
                    }

                    try (PreparedStatement insert = con.prepareStatement("INSERT INTO purchases(user_id,game_id,amount,status) VALUES(?,?,?,?)")) {
                        insert.setInt(1, userId);
                        insert.setInt(2, gameId);
                        insert.setDouble(3, price);
                        insert.setString(4, "SUCCESS");
                        insert.executeUpdate();
                    }

                    con.commit();
                    System.out.println("Game Purchased Successfully!");
                }
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}