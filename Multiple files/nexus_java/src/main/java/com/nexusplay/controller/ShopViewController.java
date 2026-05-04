package com.nexusplay.controller;

import com.nexusplay.dao.ProductDAO;
import com.nexusplay.entity.Product;
import com.nexusplay.util.FlowLayoutHelper;
import com.nexusplay.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShopViewController extends BaseController {






    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label avatarLabel;
    @FXML private VBox mainContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane productsFlowPane;
    @FXML private Button navHome;
    @FXML private Button navGames;
    @FXML private Button navShop;
    @FXML private Button navForum;
    @FXML private Button navContent;
    @FXML private Button navStreams;
    @FXML private Button navPlayers;
    @FXML private Button navCoaches;
    @FXML private Button navOrganizations;
    @FXML private Button navTeams;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageLabel;

    private int currentPage = 0;
    private static final int PRODUCTS_PER_PAGE = 8;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        bindWindowChrome();
        FlowLayoutHelper.bindPrefWrapLengthToRegion(mainContent, productsFlowPane, 80);
        loadUserInfo();
        loadProducts();
    }

    private void loadUserInfo() {
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "User");
            userTypeLabel.setText(currentUser.getUserType() != null ? currentUser.getUserType().name() : "Visitor");
            avatarLabel.setText(currentUser.getUsername() != null && currentUser.getUsername().length() > 0 
                ? currentUser.getUsername().substring(0, 1).toUpperCase() : "U");
        }
    }

    private void loadProducts() {
        ProductDAO productDAO = new ProductDAO();
        java.util.List<Product> products = productDAO.findAllPaginated(currentPage, PRODUCTS_PER_PAGE);
        long totalProducts = productDAO.countAll();
        totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);
        
        productsFlowPane.getChildren().clear();
        
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                if (product.getDeletedAt() == null) {
                    productsFlowPane.getChildren().add(createProductCard(product));
                }
            }
        } else {
            Label noProductsLabel = new Label("No products available");
            noProductsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 16px;");
            productsFlowPane.getChildren().add(noProductsLabel);
        }
        
        updatePaginationControls();
    }
    
    private void updatePaginationControls() {
        if (pageLabel != null) {
            pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        }
        if (prevPageButton != null) {
            prevPageButton.setDisable(currentPage == 0);
        }
        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages - 1 || totalPages == 0);
        }
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadProducts();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadProducts();
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1A1D26; -fx-background-radius: 12; -fx-padding: 16; -fx-spacing: 12;");
        card.setPrefWidth(220);
        
        // Product image
        ImageView imageView = new ImageView();
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Image image = new Image(product.getImagePath(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Use default placeholder if image fails to load
            }
        }
        imageView.setFitHeight(140);
        imageView.setFitWidth(188);
        imageView.setPreserveRatio(false);
        imageView.setStyle("-fx-background-radius: 8; -fx-background-color: #252936;");
        
        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        
        // Product type
        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        
        // Product description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            Label descLabel = new Label(product.getDescription());
            descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 11px;");
            descLabel.setWrapText(true);
            descLabel.setMaxHeight(40);
            card.getChildren().addAll(imageView, nameLabel, typeLabel, descLabel);
        } else {
            card.getChildren().addAll(imageView, nameLabel, typeLabel);
        }
        
        // Price and quantity
        HBox infoBox = new HBox();
        infoBox.setSpacing(10);
        
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #FF4D2E; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label quantityLabel = new Label("Stock: " + product.getQuantity());
        quantityLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        
        infoBox.getChildren().addAll(priceLabel, quantityLabel);
        card.getChildren().add(infoBox);
        
        // Buy button
        Button buyButton = new Button("Buy Now");
        buyButton.setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-size: 13px;");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setOnAction(e -> handleBuyProduct(product));
        
        card.getChildren().add(buyButton);
        
        return card;
    }

    private void handleBuyProduct(Product product) {
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Please log in to purchase products");
            return;
        }
        
        if (product.getQuantity() <= 0) {
            showAlert("This product is out of stock");
            return;
        }
        
        // TODO: Implement actual purchase logic (create ProductPurchase, update quantity, etc.)
        showAlert("Purchase functionality coming soon for: " + product.getName());
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showGames() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/GamesView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showShop() {
        // Already on shop page
    }

    @FXML
    private void showForum() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ForumView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showContent() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ContentView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStreams() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/StreamsView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showPlayers() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/PlayersView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showCoaches() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/CoachesView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showOrganizations() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/OrganizationsView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTeams() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/TeamsView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
