package com.nexusplay.controller.crud;

import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;
import com.nexusplay.util.SceneNavigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UsersCRUDController extends CRUDController<User> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected FlowPane cardsFlowPane;
    @FXML protected Label countLabel;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();
    
    // Instance variables for dialog fields
    private TextField currentUsernameField;
    private TextField currentEmailField;
    private PasswordField currentPasswordField;
    private ComboBox<String> currentUserTypeCombo;
    private ComboBox<String> currentStatusCombo;
    private CheckBox currentHasPlayerCheck;

    @FXML
    public void initialize() {
        setupButtons();
        loadCards();
    }

    @Override
    protected void setupTableView() {
        // Not using table view for users - using cards instead
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            User newUser = createNewItem();
            showUserDialog(newUser, "Add New User");
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

    protected void loadCards() {
        allItems = loadAllItems();
        filteredUsers.clear();
        if (allItems != null) {
            filteredUsers.addAll(allItems);
        }
        countLabel.setText(filteredUsers.size() + " users");
        
        // Setup pagination
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) filteredUsers.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    protected void filterCards(String searchText) {
        String searchLower = searchText != null ? searchText.toLowerCase() : "";
        filteredUsers.clear();
        
        for (User user : allItems) {
            boolean matches = searchLower.isEmpty() || 
                (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower));
            
            if (matches) {
                filteredUsers.add(user);
            }
        }
        
        countLabel.setText(filteredUsers.size() + " users");
        
        // Setup pagination
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) filteredUsers.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }
    
    private VBox createPage(int pageIndex) {
        int itemsPerPage = 6;
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredUsers.size());
        
        VBox page = new VBox(15);
        page.setStyle("-fx-padding: 10;");
        
        FlowPane pageFlowPane = new FlowPane(15, 15);
        pageFlowPane.setStyle("-fx-padding: 10;");
        
        for (int i = fromIndex; i < toIndex; i++) {
            pageFlowPane.getChildren().add(createUserCard(filteredUsers.get(i)));
        }
        
        if (filteredUsers.isEmpty()) {
            Label emptyLabel = new Label("No users found in database.\nClick '+ Add User' button to add users manually.");
            emptyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 16px; -fx-font-style: italic; -fx-text-alignment: center;");
            pageFlowPane.getChildren().add(emptyLabel);
        }
        
        page.getChildren().add(pageFlowPane);
        return page;
    }

    protected void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to return to dashboard: " + ex.getMessage());
        }
    }

    @Override
    protected List<User> loadAllItems() {
        return userDAO.findAll();
    }

    @Override
    protected void addItem() {
        User newUser = createNewItem();
        showUserDialog(newUser, "Add New User");
    }

    @Override
    protected void editItem(User item) {
        showUserDialog(item, "Edit User");
    }

    @Override
    protected void deleteItem(User item) {
        userDAO.delete(item);
        loadCards();
    }

    @Override
    protected User createNewItem() {
        User user = new User();
        user.setStatus(User.UserStatus.ACTIVE);
        user.setUserType(User.UserType.REGISTERED);
        user.setHasPlayer(false);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return user;
    }

    @Override
    protected void updateItem(User item) {
        // Dialog will update the item
    }

    @Override
    protected boolean saveItem(User item) {
        try {
            if (item.getId() == null) {
                userDAO.save(item);
            } else {
                userDAO.update(item);
            }
            return true;
        } catch (Exception e) {
            showError("Error saving user: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected VBox createDialogContent(User item) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #1A1D26;");

        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        currentUsernameField = new TextField(item.getUsername());
        currentUsernameField.setPromptText("Username");
        currentUsernameField.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.40); -fx-background-radius: 8; -fx-border-color: #3D2FA0; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        currentUsernameField.setPrefWidth(350);

        // Email field
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        currentEmailField = new TextField(item.getEmail());
        currentEmailField.setPromptText("Email");
        currentEmailField.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.40); -fx-background-radius: 8; -fx-border-color: #3D2FA0; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        currentEmailField.setPrefWidth(350);

        // Password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Password (leave blank to keep existing)");
        currentPasswordField.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.40); -fx-background-radius: 8; -fx-border-color: #3D2FA0; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        currentPasswordField.setPrefWidth(350);

        // User type combo
        Label userTypeLabel = new Label("User Type");
        userTypeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        currentUserTypeCombo = new ComboBox<>();
        currentUserTypeCombo.getItems().addAll("ADMIN", "COACH", "ORGANIZATION", "REGISTERED", "VISITOR");
        currentUserTypeCombo.setValue(item.getUserType() != null ? item.getUserType().toString() : "REGISTERED");
        currentUserTypeCombo.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: #3D2FA0; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        currentUserTypeCombo.setPrefWidth(350);

        // Status combo
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        currentStatusCombo = new ComboBox<>();
        currentStatusCombo.getItems().addAll("ACTIVE", "BANNED");
        currentStatusCombo.setValue(item.getStatus() != null ? item.getStatus().toString() : "ACTIVE");
        currentStatusCombo.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: #3D2FA0; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        currentStatusCombo.setPrefWidth(350);

        // Has player checkbox
        currentHasPlayerCheck = new CheckBox("Has Player Profile");
        currentHasPlayerCheck.setSelected(item.getHasPlayer() != null ? item.getHasPlayer() : false);
        currentHasPlayerCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        content.getChildren().addAll(
            usernameLabel, currentUsernameField,
            emailLabel, currentEmailField,
            passwordLabel, currentPasswordField,
            userTypeLabel, currentUserTypeCombo,
            statusLabel, currentStatusCombo,
            currentHasPlayerCheck
        );

        return content;
    }

    @Override
    protected boolean validateInput(User item) {
        String username = currentUsernameField.getText().trim();
        String email = currentEmailField.getText().trim();
        String password = currentPasswordField.getText().trim();

        if (username.isEmpty()) {
            showError("Username is required");
            return false;
        }

        if (username.length() < 4) {
            showError("Username must be at least 4 characters");
            return false;
        }

        if (username.length() > 10000) {
            showError("Username must be less than 10000 characters");
            return false;
        }

        if (email.isEmpty()) {
            showError("Email is required");
            return false;
        }

        if (email.length() < 4) {
            showError("Email must be at least 4 characters");
            return false;
        }

        if (email.length() > 10000) {
            showError("Email must be less than 10000 characters");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Email must be in valid email format");
            return false;
        }

        if (item.getId() == null && password.isEmpty()) {
            showError("Password is required for new users");
            return false;
        }

        if (!password.isEmpty()) {
            if (password.length() < 4) {
                showError("Password must be at least 4 characters");
                return false;
            }

            if (password.length() > 10000) {
                showError("Password must be less than 10000 characters");
                return false;
            }
        }

        // Update the user object
        item.setUsername(username);
        item.setEmail(email);
        if (!password.isEmpty()) {
            item.setPassword(password);
        }
        item.setUserType(User.UserType.valueOf(currentUserTypeCombo.getValue()));
        item.setStatus(User.UserStatus.valueOf(currentStatusCombo.getValue()));
        item.setHasPlayer(currentHasPlayerCheck.isSelected());

        return true;
    }

    protected void displayUsersCards() {
        cardsFlowPane.getChildren().clear();
        
        if (filteredUsers.isEmpty()) {
            Label emptyLabel = new Label("No users found in database.\nClick '+ Add User' button to add users manually.");
            emptyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 16px; -fx-font-style: italic; -fx-text-alignment: center;");
            cardsFlowPane.getChildren().add(emptyLabel);
            return;
        }
        
        for (User user : filteredUsers) {
            cardsFlowPane.getChildren().add(createUserCard(user));
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12;");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header with avatar and name
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(50, 50);
        avatarPane.setStyle("-fx-background-color: #3D2FA0; -fx-background-radius: 25;");
        
        Label avatarLabel = new Label(user.getUsername() != null ? user.getUsername().substring(0, 1).toUpperCase() : "U");
        avatarLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        avatarPane.getChildren().add(avatarLabel);

        // User info
        VBox userInfoBox = new VBox(2);
        Label nameLabel = new Label(user.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px;");
        emailLabel.setWrapText(true);

        userInfoBox.getChildren().addAll(nameLabel, emailLabel);

        headerBox.getChildren().addAll(avatarPane, userInfoBox);

        // User type and status badges
        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(user.getUserType() != null ? user.getUserType().toString() : "REGISTERED");
        typeBadge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4; -fx-background-color: " + getTypeColor(user.getUserType()) + ";");

        Label statusBadge = new Label(user.getStatus() != null ? user.getStatus().toString() : "ACTIVE");
        statusBadge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4; -fx-background-color: " + getStatusColor(user.getStatus()) + ";");

        badgesBox.getChildren().addAll(typeBadge, statusBadge);

        // Additional info
        Label createdLabel = new Label("Joined: " + (user.getCreatedAt() != null ? 
            user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown"));
        createdLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 10px;");

        // Action buttons
        HBox actionsBox = new HBox(5);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        editBtn.setOnAction(e -> editItem(user));

        Button blockBtn = new Button(user.getStatus() == User.UserStatus.BANNED ? "Unblock" : "Block");
        blockBtn.setStyle("-fx-background-color: " + (user.getStatus() == User.UserStatus.BANNED ? "#10B981" : "#F59E0B") + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        blockBtn.setOnAction(e -> toggleBlockUser(user));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        deleteBtn.setOnAction(e -> deleteItem(user));

        actionsBox.getChildren().addAll(editBtn, blockBtn, deleteBtn);

        // Add all components to card
        card.getChildren().addAll(headerBox, badgesBox, createdLabel, actionsBox);

        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #3D2FA0; -fx-border-radius: 12;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12;"));

        return card;
    }

    private String getTypeColor(User.UserType userType) {
        if (userType == null) return "#6B7280";
        switch (userType) {
            case ADMIN: return "#DC2626";
            case COACH: return "#059669";
            case ORGANIZATION: return "#7C3AED";
            case REGISTERED: return "#2563EB";
            case VISITOR: return "#6B7280";
            default: return "#6B7280";
        }
    }

    private String getStatusColor(User.UserStatus status) {
        if (status == null) return "#6B7280";
        switch (status) {
            case ACTIVE: return "#10B981";
            case BANNED: return "#E74C3C";
            default: return "#6B7280";
        }
    }

    private void toggleBlockUser(User user) {
        if (user.getStatus() == User.UserStatus.BANNED) {
            user.setStatus(User.UserStatus.ACTIVE);
        } else {
            user.setStatus(User.UserStatus.BANNED);
        }
        saveItem(user);
        loadCards();
    }

    private void showUserDialog(User user, String title) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        
        // Style the dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #1A1D26;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style the dialog buttons
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #3D2FA0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-size: 13px;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color: #252936; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-size: 13px; -fx-border-color: #3D2FA0; -fx-border-radius: 6;");

        VBox content = createDialogContent(user);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(user)) {
                    return user;
                }
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(savedUser -> {
            if (saveItem(savedUser)) {
                loadCards();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
