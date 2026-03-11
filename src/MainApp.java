import javafx.animation.ScaleTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private User currentUser;
    private Stage primaryStage;
    private final UserDAO userDAO = new UserDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final PurchaseService purchaseService = new PurchaseService();

    private final ObservableList<Game> games = FXCollections.observableArrayList();
    private final List<Game> cart = new ArrayList<>();
    private List<Purchase> ownedGames = new ArrayList<>();
    
    // UI tracking
    private Label cartLabel;
    private StackPane rootPane;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("GameVersePro");

        Scene loginScene = buildLoginScreen();
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene buildLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.getStyleClass().add("root-bg");

        VBox loginCard = new VBox(20);
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(javafx.geometry.Pos.CENTER);
        loginCard.setMaxWidth(400);

        Label appTitle = new Label("GameVersePro");
        appTitle.getStyleClass().add("app-title");
        appTitle.setStyle("-fx-font-size: 36px; -fx-padding: 0 0 10 0;");

        Label subtitle = new Label("Sign in to access the store");
        subtitle.getStyleClass().add("user-info");

        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        userField.getStyleClass().add("input-field");
        userField.setMaxWidth(300);

        HBox authButtons = new HBox(15);
        authButtons.setAlignment(javafx.geometry.Pos.CENTER);
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().addAll("btn", "btn-primary");
        loginBtn.setPrefWidth(120);

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().addAll("btn", "btn-secondary");
        registerBtn.setPrefWidth(120);

        authButtons.getChildren().addAll(loginBtn, registerBtn);

        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            if (username.isEmpty()) { showAlert("Error", "Enter username"); return; }
            try {
                User u = userDAO.findByUsername(username);
                if (u == null) { showAlert("Error", "User not found. Please register."); return; }
                currentUser = u;
                loadUserOwnedGames();
                primaryStage.setScene(buildMainScreen());
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });

        registerBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            if (username.isEmpty()) { showAlert("Error", "Enter username"); return; }
            try {
                if (userDAO.findByUsername(username) != null) { showAlert("Error", "Username taken"); return; }
                currentUser = userDAO.create(username, 50.0);
                showAlert("Success", "Registered successfully");
                loadUserOwnedGames();
                primaryStage.setScene(buildMainScreen());
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });

        loginCard.getChildren().addAll(appTitle, subtitle, userField, authButtons);
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css") != null ? getClass().getResource("/style.css").toExternalForm() : "file:style.css");
        return scene;
    }

    private void loadUserOwnedGames() {
        if (currentUser != null) {
            try {
                ownedGames = purchaseDAO.getPurchasesByUser(currentUser.getUserId());
            } catch (Exception e) {}
        }
    }

    private boolean isGameOwned(int gameId) {
        for (Purchase p : ownedGames) {
            if (p.getGameId() == gameId) return true;
        }
        return false;
    }

    private Scene buildMainScreen() {
        cart.clear(); // Reset cart on login

        rootPane = new StackPane();
        HBox mainLayout = new HBox();
        mainLayout.getStyleClass().add("root-bg");
        Scene scene = new Scene(rootPane, 1000, 600);
        scene.getStylesheets()
                .add(getClass().getResource("/style.css") != null
                        ? getClass().getResource("/style.css").toExternalForm()
                        : "file:style.css");

        // --- Sidebar (Left Panel) ---
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        Label appTitle = new Label("GameVersePro");
        appTitle.getStyleClass().add("app-title");

        VBox userSection = new VBox(10);
        userSection.getStyleClass().add("user-section");
        Label userInfo = new Label("Logged in as: " + currentUser.getUsername() + "\nBalance: $" + currentUser.getWallet());
        userInfo.getStyleClass().add("user-info");

        // Auth buttons are gone from sidebar, replaced with Logout
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("btn", "btn-secondary");
        
        userSection.getChildren().addAll(userInfo, logoutBtn);

        VBox actionSection = new VBox(10);
        Button refreshBtn = new Button("Refresh Store");
        refreshBtn.getStyleClass().addAll("btn", "btn-action");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        Button topUpBtn = new Button("Top-up Wallet");
        topUpBtn.getStyleClass().addAll("btn", "btn-action");
        topUpBtn.setMaxWidth(Double.MAX_VALUE);
        actionSection.getChildren().addAll(refreshBtn, topUpBtn);

        sidebar.getChildren().addAll(appTitle, userSection, actionSection);

        // --- Main Content (Right Panel) ---
        VBox mainContent = new VBox(20);
        mainContent.getStyleClass().add("main-content");
        mainContent.setPrefWidth(750);
        HBox.setHgrow(mainContent, javafx.scene.layout.Priority.ALWAYS);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label storeLabel = new Label("Store");
        storeLabel.getStyleClass().add("section-header");
        
        cartLabel = new Label("Cart: 0 items");
        cartLabel.getStyleClass().add("cart-label");
        
        Button checkoutBtn = new Button("Checkout Cart");
        checkoutBtn.getStyleClass().addAll("btn", "btn-success");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        headerBox.getChildren().addAll(storeLabel, spacer, cartLabel, checkoutBtn);

        // Games Grid View using TilePane
        javafx.scene.layout.TilePane gamesGrid = new javafx.scene.layout.TilePane();
        gamesGrid.getStyleClass().add("games-grid");
        gamesGrid.setHgap(15);
        gamesGrid.setVgap(15);
        gamesGrid.setPrefColumns(3);

        ScrollPane scrollPane = new ScrollPane(gamesGrid);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, scrollPane);
        
        mainLayout.getChildren().addAll(sidebar, mainContent);
        rootPane.getChildren().add(mainLayout);

        // Sidebar Actions
        logoutBtn.setOnAction(e -> {
            currentUser = null;
            ownedGames.clear();
            primaryStage.setScene(buildLoginScreen());
        });

        refreshBtn.setOnAction(e -> {
            loadUserOwnedGames();
            loadGames();
            userInfo.setText("Logged in as: " + currentUser.getUsername() + "\nBalance: $" + currentUser.getWallet());
        });

        checkoutBtn.setOnAction(e -> processCheckout(userInfo));

        topUpBtn.setOnAction(e -> {
            if (currentUser == null) {
                showAlert("Top-up", "Login first");
                return;
            }
            TextInputDialog d = new TextInputDialog("10");
            d.setHeaderText("Amount to add");
            d.showAndWait().ifPresent(s -> {
                try {
                    double amt = Double.parseDouble(s);
                    try (var c = DBConnection.getConnection()) {
                        c.setAutoCommit(false);
                        new UserDAO().updateWalletDelta(c, currentUser.getUserId(), amt);
                        c.commit();
                    }
                    currentUser = userDAO.findById(currentUser.getUserId());
                    userInfo.setText("User: " + currentUser.getUsername() + " | Wallet: $" + currentUser.getWallet());
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            });
        });

        BuyBtnRef = checkoutBtn;
        primaryStage.setScene(scene);
        primaryStage.show();

        loadGames();
        return scene;
    }

    private void processCheckout(Label userInfo) {
        if (cart.isEmpty()) {
            showAlert("Cart Empty", "Please add items to your cart first.");
            return;
        }

        double totalCost = 0;
        for (Game g : cart) totalCost += g.getPrice();

        if (currentUser.getWallet() < totalCost) {
            showAlert("Checkout Failed", "Insufficient funds. Total is $" + totalCost);
            return;
        }

        try {
            // Process all purchases in cart sequentially
            for (Game g : cart) {
                purchaseService.buyGame(currentUser.getUserId(), g.getGameId());
            }
            
            // Re-fetch user to reflect new balance & owned games
            currentUser = userDAO.findByUsername(currentUser.getUsername());
            userInfo.setText("Logged in as: " + currentUser.getUsername() + "\nBalance: $" + currentUser.getWallet());
            loadUserOwnedGames();
            
            // Clear cart & redraw games grid
            cart.clear();
            cartLabel.setText("Cart: 0 items");
            loadGames();
            
            // Show custom PhonePe style success animation overlay!
            showPaymentSuccessAnimation();
            
        } catch (Exception ex) {
            showAlert("Checkout Error", ex.getMessage());
        }
    }
    
    // --- Awesome Animation Sequence ---
    private void showPaymentSuccessAnimation() {
        VBox overlay = new VBox(20);
        overlay.setAlignment(javafx.geometry.Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        
        StackPane checkCircle = new StackPane();
        Circle circle = new Circle(60, javafx.scene.paint.Color.valueOf("#10b981")); // Success Green
        
        // CSS SVG Path for a checkmark
        SVGPath checkPath = new SVGPath();
        checkPath.setContent("M 25 60 L 45 80 L 95 30"); // Simple tick path inside 120x120 bounds approx
        checkPath.setStroke(javafx.scene.paint.Color.WHITE);
        checkPath.setStrokeWidth(8);
        checkPath.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        checkPath.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        checkPath.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        checkCircle.getChildren().addAll(circle, checkPath);
        
        Label lbl = new Label("Payment Successful");
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        overlay.getChildren().addAll(checkCircle, lbl);
        
        // Start Small and hidden
        checkCircle.setScaleX(0);
        checkCircle.setScaleY(0);
        
        rootPane.getChildren().add(overlay);
        
        // Pop animation
        ScaleTransition st = new ScaleTransition(Duration.millis(500), checkCircle);
        st.setFromX(0); st.setFromY(0);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        st.setOnFinished(e -> {
            // Keep it visible for a moment then fade out the overlay entirely
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(ev -> rootPane.getChildren().remove(overlay));
            pause.play();
        });
        
        st.play();
    }

    private void loadGames() {
        try {
            List<Game> list = gameDAO.listAll();
            games.setAll(list);

            // Rebuild visual grid representation
            javafx.scene.Scene scene = games.isEmpty() ? null : BuyBtnRef.getScene();
            if (scene != null) {
                javafx.scene.layout.TilePane gamesGrid = (javafx.scene.layout.TilePane) scene.lookup(".games-grid");
                if (gamesGrid != null) {
                    gamesGrid.getChildren().clear();
                    for (Game g : list) {
                        VBox card = new VBox(10);
                        card.getStyleClass().add("game-card");

                        javafx.scene.image.ImageView cover = new javafx.scene.image.ImageView();
                        cover.setFitWidth(180);
                        cover.setFitHeight(100);
                        cover.setPreserveRatio(true);
                        javafx.scene.image.Image img = getGameImage(g.getTitle());
                        if (img != null) {
                            cover.setImage(img);
                        }

                        Label title = new Label(g.getTitle());
                        title.getStyleClass().add("game-title");
                        title.setWrapText(true);

                        boolean isOwned = isGameOwned(g.getGameId());

                        if (isOwned) {
                            Label lblPurchased = new Label("✓ Purchased");
                            lblPurchased.getStyleClass().add("game-purchased-lbl");
                            
                            Button btnDownload = new Button("Download");
                            btnDownload.getStyleClass().addAll("btn", "btn-play");
                            btnDownload.setOnAction(ev -> showAlert("Download", "Downloading " + g.getTitle() + "...\nReady to play!"));
                            
                            card.getChildren().addAll(cover, title, lblPurchased, btnDownload);
                        } else {
                            Label priceInfo = new Label(String.format("$%.2f", g.getPrice()));
                            priceInfo.getStyleClass().add("game-price");
                            
                            Label stockInfo = new Label(g.getStock() > 0 ? "In Stock: " + g.getStock() : "Out of Stock");
                            stockInfo.getStyleClass().add(g.getStock() > 0 ? "game-stock" : "game-out-stock");
                            
                            Button btnAddCart = new Button("Add to Cart");
                            btnAddCart.getStyleClass().addAll("btn", "btn-action");
                            if (g.getStock() <= 0) btnAddCart.setDisable(true);
                            
                            btnAddCart.setOnAction(ev -> {
                                if (!cart.contains(g)) {
                                    cart.add(g);
                                    cartLabel.setText("Cart: " + cart.size() + " items");
                                    btnAddCart.setText("In Cart");
                                    btnAddCart.setDisable(true);
                                }
                            });
                            
                            card.getChildren().addAll(cover, title, priceInfo, stockInfo, btnAddCart);
                        }

                        // Just standard hover tracking now instead of explicit selection array
                        card.setOnMouseEntered(ev -> card.getStyleClass().add("game-card-hover"));
                        card.setOnMouseExited(ev -> card.getStyleClass().remove("game-card-hover"));

                        gamesGrid.getChildren().add(card);
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    // Reference to scene/root locating
    private Button BuyBtnRef;

    private javafx.scene.image.Image getGameImage(String title) {
        String t = title.toLowerCase();
        String file = "cyberpunk.jpg"; // default fallback

        if (t.contains("assassin") || t.contains("mirage"))
            file = "acmirage.jpg";
        else if (t.contains("apex"))
            file = "apex.jpg";
        else if (t.contains("baldur"))
            file = "baldurs3.jpg";
        else if (t.contains("call of duty") || t.contains("mw3"))
            file = "codmw3.jpg";
        else if (t.contains("cyberpunk"))
            file = "cyberpunk.jpg";
        else if (t.contains("elden"))
            file = "eldenring.jpg";
        else if (t.contains("fifa") || t.contains("fc 24"))
            file = "fifa24.jpg";
        else if (t.contains("fortnite"))
            file = "fortnite.jpg";
        else if (t.contains("forza"))
            file = "forza5.jpg";
        else if (t.contains("god of war"))
            file = "godofwar.jpg";
        else if (t.contains("gta") || t.contains("grand theft"))
            file = "gta5.jpg";
        else if (t.contains("hogwarts") || t.contains("harry potter"))
            file = "hogwarts.jpg";
        else if (t.contains("minecraft"))
            file = "minecraft.jpg";
        else if (t.contains("red dead") || t.contains("rdr2"))
            file = "rdr2.jpg";
        else if (t.contains("resident evil") || t.contains("re4"))
            file = "re4.jpg";
        else if (t.contains("sekiro"))
            file = "sekiro.jpg";
        else if (t.contains("spider-man") || t.contains("spiderman"))
            file = "spiderman.jpg";
        else if (t.contains("starfield"))
            file = "starfield.jpg";
        else if (t.contains("valorant"))
            file = "valorant.jpg";
        else if (t.contains("witcher"))
            file = "witcher3.jpg";

        try {
            java.io.File f = new java.io.File("images/" + file);
            if (f.exists()) {
                return new javafx.scene.image.Image(f.toURI().toString(), true);
            }
        } catch (Exception e) {
        }

        return null;
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
