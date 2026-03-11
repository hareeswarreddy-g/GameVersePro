public class User {
    private int userId;
    private String username;
    private double wallet;

    public User() {}

    public User(int userId, String username, double wallet) {
        this.userId = userId;
        this.username = username;
        this.wallet = wallet;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getWallet() { return wallet; }
    public void setWallet(double wallet) { this.wallet = wallet; }
}
