package com.nexusplay.controller.crud;

import com.nexusplay.dao.CoachDAO;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.Coach;
import com.nexusplay.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CoachesCRUDController extends CRUDController<Coach> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private CoachDAO coachDAO = new CoachDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Coach> filteredCoaches = FXCollections.observableArrayList();
    
    private ComboBox<User> currentUserCombo;
    private TextField currentExperienceField;
    private TextArea currentBioField;
    private TextField currentRatingField;
    private TextField currentPriceField;

    @FXML
    public void initialize() {
        loadUsers();
        setupButtons();
        loadCards();
    }

    protected void loadUsers() {
        try {
            List<User> userList = userDAO.findAll();
            users.clear();
            if (userList != null) {
                users.addAll(userList);
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Coach newCoach = createNewItem();
            showCoachDialog(newCoach, "Add New Coach");
        });
        
        btnEdit.setOnAction(e -> {
            showEditDialog();
        });
        
        btnDelete.setOnAction(e -> {
            showDeleteDialog();
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
    
    private void showEditDialog() {
        if (filteredCoaches.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Coaches");
            alert.setHeaderText(null);
            alert.setContentText("No coaches found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Coach> coachesToEdit = filteredCoaches.isEmpty() ? allItems : filteredCoaches;
        
        ChoiceDialog<Coach> dialog = new ChoiceDialog<>(coachesToEdit.get(0), coachesToEdit);
        dialog.setTitle("Edit Coach");
        dialog.setHeaderText("Select a coach to edit");
        dialog.setContentText("Choose a coach from the list:");
        
        dialog.showAndWait().ifPresent(coach -> {
            editItem(coach);
        });
    }
    
    private void showDeleteDialog() {
        if (filteredCoaches.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Coaches");
            alert.setHeaderText(null);
            alert.setContentText("No coaches found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Coach> coachesToDelete = filteredCoaches.isEmpty() ? allItems : filteredCoaches;
        
        ChoiceDialog<Coach> dialog = new ChoiceDialog<>(coachesToDelete.get(0), coachesToDelete);
        dialog.setTitle("Delete Coach");
        dialog.setHeaderText("Select a coach to delete");
        dialog.setContentText("Choose a coach from the list:");
        
        dialog.showAndWait().ifPresent(coach -> {
            deleteItem(coach);
        });
    }

    protected void goBackToDashboard() {
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
    protected List<Coach> loadAllItems() {
        return coachDAO.findAll();
    }

    @Override
    protected void deleteItem(Coach item) {
        coachDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Coach newCoach = createNewItem();
        showCoachDialog(newCoach, "Add New Coach");
    }

    @Override
    protected void editItem(Coach item) {
        showCoachDialog(item, "Edit Coach");
    }

    @Override
    protected Coach createNewItem() {
        Coach coach = new Coach();
        coach.setCreatedAt(java.time.LocalDateTime.now());
        return coach;
    }

    @Override
    protected void updateItem(Coach item) {
    }

    @Override
    protected boolean saveItem(Coach item) {
        try {
            if (item.getId() == null) {
                coachDAO.save(item);
            } else {
                coachDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving coach: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        List<Coach> coaches = loadAllItems();
        filteredCoaches.clear();
        if (coaches != null) {
            filteredCoaches.addAll(coaches);
        }
        
        int pageCount = (int) Math.ceil((double) filteredCoaches.size() / 6);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        countLabel.setText(filteredCoaches.size() + " coaches");
    }

    protected void filterCards(String searchText) {
        List<Coach> allCoaches = loadAllItems();
        filteredCoaches.clear();
        
        if (allCoaches != null) {
            for (Coach coach : allCoaches) {
                if (coach.getUser() != null && coach.getUser().getUsername() != null && 
                    coach.getUser().getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredCoaches.add(coach);
                }
            }
        }
        
        int pageCount = (int) Math.ceil((double) filteredCoaches.size() / 6);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        countLabel.setText(filteredCoaches.size() + " coaches");
    }
    
    private FlowPane createPage(int pageIndex) {
        FlowPane page = new FlowPane(15, 15);
        page.setStyle("-fx-padding: 10;");
        
        int fromIndex = pageIndex * 6;
        int toIndex = Math.min(fromIndex + 6, filteredCoaches.size());
        
        for (int i = fromIndex; i < toIndex; i++) {
            page.getChildren().add(createCoachCard(filteredCoaches.get(i)));
        }
        
        return page;
    }

    protected VBox createCoachCard(Coach coach) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Coach icon
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setStyle("-fx-background-color: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); -fx-background-radius: 8;");
        iconPane.setPrefSize(250, 100);
        iconPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label coachIcon = new Label("🏆");
        coachIcon.setStyle("-fx-font-size: 48px;");
        iconPane.getChildren().add(coachIcon);
        
        // Coach name
        String coachName = "Coach #" + coach.getId();
        try {
            if (coach.getUser() != null && coach.getUser().getUsername() != null) {
                coachName = coach.getUser().getUsername();
            }
        } catch (Exception e) {
            coachName = "Coach #" + coach.getId();
        }
        Label nameLabel = new Label(coachName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 5 0;");
        
        // Experience
        Label experienceLabel = new Label(coach.getExperienceLevel() != null ? "⭐ " + coach.getExperienceLevel() : "⭐ Experience: N/A");
        experienceLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Rating
        Label ratingLabel = new Label(coach.getRating() != null ? "⭐ Rating: " + coach.getRating() + "/5.0" : "⭐ Rating: N/A");
        ratingLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Price
        Label priceLabel = new Label(coach.getPricePerSession() != null ? "💰 $" + coach.getPricePerSession() + "/session" : "💰 Price: N/A");
        priceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Bio preview
        Label bioLabel = new Label(coach.getBio() != null && coach.getBio().length() > 50 ? 
            coach.getBio().substring(0, 50) + "..." : coach.getBio());
        bioLabel.setStyle("-fx-text-fill: #E0E7FF; -fx-font-size: 11px; -fx-wrap-text: true;");
        bioLabel.setPrefWidth(250);
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(coach));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(coach));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(iconPane, nameLabel, experienceLabel, ratingLabel, priceLabel, bioLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #f093fb; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Coach item) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label(item.getId() == null ? "Add New Coach" : "Edit Coach");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        currentUserCombo = new ComboBox<>(users);
        currentUserCombo.setValue(item.getUser());
        currentUserCombo.setPromptText("Select User");
        currentUserCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentUserCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "Select User";
                try {
                    String username = user.getUsername();
                    return username != null ? username : "User";
                } catch (Exception e) {
                    return "User";
                }
            }
            @Override
            public User fromString(String string) {
                return null;
            }
        });
        
        currentExperienceField = new TextField(item.getExperienceLevel());
        currentExperienceField.setPromptText("Experience Level");
        currentExperienceField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentBioField = new TextArea(item.getBio());
        currentBioField.setPromptText("Bio/Description");
        currentBioField.setPrefRowCount(3);
        currentBioField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: #E0E7FF; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentRatingField = new TextField(item.getRating() != null ? item.getRating().toString() : "");
        currentRatingField.setPromptText("Rating (0.0 - 5.0)");
        currentRatingField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentPriceField = new TextField(item.getPricePerSession() != null ? item.getPricePerSession().toString() : "");
        currentPriceField.setPromptText("Price per Session");
        currentPriceField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        Label userLabel = new Label("Coach User:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label experienceLabel = new Label("Experience Level:");
        experienceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label bioLabel = new Label("Bio:");
        bioLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label ratingLabel = new Label("Rating:");
        ratingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label priceLabel = new Label("Price per Session:");
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            titleLabel,
            new javafx.scene.layout.Region(),
            userLabel, currentUserCombo,
            experienceLabel, currentExperienceField,
            bioLabel, currentBioField,
            ratingLabel, currentRatingField,
            priceLabel, currentPriceField
        );

        return content;
    }

    @Override
    protected boolean validateInput(Coach item) {
        if (currentUserCombo == null || currentUserCombo.getValue() == null) {
            showNotImplemented("User selection is required");
            return false;
        }

        String experienceLevel = currentExperienceField.getText().trim();
        String bio = currentBioField.getText().trim();
        String ratingText = currentRatingField.getText().trim();
        String priceText = currentPriceField.getText().trim();

        // Validate Experience Level
        if (experienceLevel.isEmpty()) {
            showNotImplemented("Experience Level is required");
            return false;
        }
        if (experienceLevel.length() < 4) {
            showNotImplemented("Experience Level must be at least 4 characters");
            return false;
        }
        if (experienceLevel.length() > 10000) {
            showNotImplemented("Experience Level must be less than 10000 characters");
            return false;
        }

        // Validate Bio
        if (bio.isEmpty()) {
            showNotImplemented("Bio is required");
            return false;
        }
        if (bio.length() < 4) {
            showNotImplemented("Bio must be at least 4 characters");
            return false;
        }
        if (bio.length() > 10000) {
            showNotImplemented("Bio must be less than 10000 characters");
            return false;
        }

        // Validate Rating
        if (ratingText.isEmpty()) {
            showNotImplemented("Rating is required");
            return false;
        }
        if (ratingText.length() < 4) {
            showNotImplemented("Rating must be at least 4 characters");
            return false;
        }
        if (ratingText.length() > 10000) {
            showNotImplemented("Rating must be less than 10000 characters");
            return false;
        }

        // Validate Price
        if (priceText.isEmpty()) {
            showNotImplemented("Price is required");
            return false;
        }
        if (priceText.length() < 4) {
            showNotImplemented("Price must be at least 4 characters");
            return false;
        }
        if (priceText.length() > 10000) {
            showNotImplemented("Price must be less than 10000 characters");
            return false;
        }

        try {
            item.setUser(currentUserCombo.getValue());
            item.setExperienceLevel(experienceLevel);
            item.setBio(bio);
            
            double rating = Double.parseDouble(ratingText);
            if (rating < 0 || rating > 5) {
                showNotImplemented("Rating must be between 0.0 and 5.0");
                return false;
            }
            item.setRating(BigDecimal.valueOf(rating));
            
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showNotImplemented("Price cannot be negative");
                return false;
            }
            item.setPricePerSession(BigDecimal.valueOf(price));
        } catch (NumberFormatException e) {
            showNotImplemented("Rating and Price must be valid numbers");
            return false;
        }

        return true;
    }

    private void showCoachDialog(Coach coach, String title) {
        // Load users before showing dialog to avoid lazy loading errors
        loadUsers();
        
        Dialog<Coach> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style the buttons
        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(coach);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(coach)) {
                    return coach;
                }
            }
            return null;
        });

        Optional<Coach> result = dialog.showAndWait();
        result.ifPresent(savedCoach -> {
            if (saveItem(savedCoach)) {
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
