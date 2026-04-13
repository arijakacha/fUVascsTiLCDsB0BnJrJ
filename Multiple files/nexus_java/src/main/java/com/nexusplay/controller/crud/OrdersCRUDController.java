package com.nexusplay.controller.crud;

import com.nexusplay.entity.UserOrder;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.dao.OrderDAO;
import com.nexusplay.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class OrdersCRUDController extends CRUDController<UserOrder> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnView;
    @FXML protected Button btnSearch;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox buttonsView;
    @FXML protected VBox listView;
    @FXML protected FlowPane cardsFlowPane;
    @FXML protected Label countLabel;

    private OrderDAO orderDAO = new OrderDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> users = FXCollections.observableArrayList();
    
    private TextField currentTotalField;
    private ComboBox<String> currentStatusCombo;
    private ComboBox<User> currentUserCombo;

    @FXML
    public void initialize() {
        loadUsers();
        setupButtons();
    }

    protected void loadUsers() {
        users.addAll(userDAO.findAll());
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            UserOrder newOrder = createNewItem();
            showOrderDialog(newOrder, "Add New Order");
        });
        
        btnView.setOnAction(e -> {
            showListView();
            loadCards();
        });
        
        btnSearch.setOnAction(e -> {
            showListView();
            searchField.requestFocus();
        });
        
        btnRefresh.setOnAction(e -> {
            loadCards();
        });
        
        btnBack.setOnAction(e -> {
            goBackToDashboard();
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCards(newVal);
        });
    }

    protected void showListView() {
        buttonsView.setVisible(false);
        listView.setVisible(true);
        pagination.setVisible(true);
    }

    protected void hideListView() {
        buttonsView.setVisible(true);
        listView.setVisible(false);
        pagination.setVisible(false);
    }

    protected void goBackToDashboard() {
        hideListView();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setupTableView() {
        // Not using table view - using cards instead
    }

    @Override
    protected List<UserOrder> loadAllItems() {
        return orderDAO.findAll();
    }

    @Override
    protected void deleteItem(UserOrder item) {
        orderDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        UserOrder newOrder = createNewItem();
        showOrderDialog(newOrder, "Add New Order");
    }

    @Override
    protected void editItem(UserOrder item) {
        showOrderDialog(item, "Edit Order");
    }

    @Override
    protected UserOrder createNewItem() {
        UserOrder order = new UserOrder();
        order.setCreatedAt(java.time.LocalDateTime.now());
        return order;
    }

    @Override
    protected void updateItem(UserOrder item) {
    }

    @Override
    protected boolean saveItem(UserOrder item) {
        try {
            if (item.getId() == null) {
                orderDAO.save(item);
            } else {
                orderDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving order: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        List<UserOrder> orders = loadAllItems();
        cardsFlowPane.getChildren().clear();
        
        for (UserOrder order : orders) {
            cardsFlowPane.getChildren().add(createOrderCard(order));
        }
        
        countLabel.setText(orders.size() + " orders");
    }

    protected void filterCards(String searchText) {
        List<UserOrder> allOrders = loadAllItems();
        cardsFlowPane.getChildren().clear();
        
        for (UserOrder order : allOrders) {
            if (order.getUser() != null && order.getUser().getUsername() != null && 
                order.getUser().getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                cardsFlowPane.getChildren().add(createOrderCard(order));
            }
        }
        
        countLabel.setText(cardsFlowPane.getChildren().size() + " orders");
    }

    protected VBox createOrderCard(UserOrder order) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Order icon
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setStyle("-fx-background-color: linear-gradient(135deg, #fc466b 0%, #3f5efb 100%); -fx-background-radius: 8;");
        iconPane.setPrefSize(250, 100);
        iconPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label orderIcon = new Label("📦");
        orderIcon.setStyle("-fx-font-size: 48px;");
        iconPane.getChildren().add(orderIcon);
        
        // Order ID
        Label idLabel = new Label("Order #" + (order.getId() != null ? order.getId() : "N/A"));
        idLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // User
        Label userLabel = new Label(order.getUser() != null ? "👤 " + order.getUser().getUsername() : "👤 User: N/A");
        userLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Total
        Label totalLabel = new Label(order.getTotal() != null ? "💰 $" + String.format("%.2f", order.getTotal()) : "💰 Total: N/A");
        totalLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Status
        Label statusLabel = new Label(order.getStatus() != null ? "📋 " + order.getStatus() : "📋 Status: N/A");
        statusLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(order));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(order));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(iconPane, idLabel, userLabel, totalLabel, statusLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #fc466b; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(UserOrder item) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        currentUserCombo = new ComboBox<>(users);
        currentUserCombo.setValue(item.getUser());
        currentUserCombo.setPromptText("Select User");

        currentTotalField = new TextField(item.getTotal() != null ? item.getTotal().toString() : "");
        currentTotalField.setPromptText("Total Amount");

        currentStatusCombo = new ComboBox<>();
        currentStatusCombo.getItems().addAll("PENDING", "COMPLETED", "CANCELLED", "REFUNDED");
        currentStatusCombo.setValue(item.getStatus());

        content.getChildren().addAll(
            new Label("User:"), currentUserCombo,
            new Label("Total:"), currentTotalField,
            new Label("Status:"), currentStatusCombo
        );

        return content;
    }

    @Override
    protected boolean validateInput(UserOrder item) {
        if (currentUserCombo.getValue() == null) {
            showNotImplemented("User selection is required");
            return false;
        }

        if (currentStatusCombo.getValue() == null || currentStatusCombo.getValue().isEmpty()) {
            showNotImplemented("Status is required");
            return false;
        }
        if (currentStatusCombo.getValue().length() < 4) {
            showNotImplemented("Status must be at least 4 characters");
            return false;
        }
        if (currentStatusCombo.getValue().length() > 10000) {
            showNotImplemented("Status must be less than 10000 characters");
            return false;
        }

        String totalText = currentTotalField == null ? "" : currentTotalField.getText().trim();
        if (totalText.isEmpty()) {
            showNotImplemented("Total is required");
            return false;
        }
        if (totalText.length() < 4) {
            showNotImplemented("Total must be at least 4 characters");
            return false;
        }
        if (totalText.length() > 10000) {
            showNotImplemented("Total must be less than 10000 characters");
            return false;
        }

        try {
            item.setUser(currentUserCombo.getValue());
            
            double total = Double.parseDouble(totalText);
            if (total < 0) {
                showNotImplemented("Total cannot be negative");
                return false;
            }
            item.setTotal(total);
            
            item.setStatus(currentStatusCombo.getValue());
        } catch (NumberFormatException e) {
            showNotImplemented("Total must be a valid number");
            return false;
        }

        return true;
    }

    private void showOrderDialog(UserOrder order, String title) {
        Dialog<UserOrder> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        VBox content = createDialogContent(order);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(order)) {
                    return order;
                }
            }
            return null;
        });

        Optional<UserOrder> result = dialog.showAndWait();
        result.ifPresent(savedOrder -> {
            if (saveItem(savedOrder)) {
            }
        });
    }

    private void showNotImplemented(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
