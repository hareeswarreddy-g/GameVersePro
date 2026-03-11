public class Game {
    private int gameId;
    private String title;
    private double price;
    private int stock;

    public Game() {}

    public Game(int gameId, String title, double price, int stock) {
        this.gameId = gameId;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
