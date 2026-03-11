import java.sql.Timestamp;

public class Purchase {
    private int purchaseId;
    private int userId;
    private int gameId;
    private double amount;
    private String status;
    private Timestamp purchasedAt;

    public Purchase() {}

    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(Timestamp purchasedAt) { this.purchasedAt = purchasedAt; }
}
