package com.nexusplay.controller.crud;

import com.nexusplay.entity.Organization;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.dao.OrganizationDAO;
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

public class OrganizationsCRUDController extends CRUDController<Organization> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private OrganizationDAO organizationDAO = new OrganizationDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Organization> filteredOrganizations = FXCollections.observableArrayList();
    
    private TextField currentNameField;
    private TextArea currentDescriptionField;
    private TextField currentLogoField;
    private ComboBox<User> currentOwnerCombo;
    private CheckBox currentValidatedCheck;

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
            Organization newOrg = createNewItem();
            showOrganizationDialog(newOrg, "Add New Organization");
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
        // Show a dialog to select an organization to edit
        if (filteredOrganizations.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Organizations");
            alert.setHeaderText(null);
            alert.setContentText("No organizations found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Organization> orgsToEdit = filteredOrganizations.isEmpty() ? allItems : filteredOrganizations;
        
        // Create a selection dialog
        ChoiceDialog<Organization> dialog = new ChoiceDialog<>(orgsToEdit.get(0), orgsToEdit);
        dialog.setTitle("Edit Organization");
        dialog.setHeaderText("Select an organization to edit");
        dialog.setContentText("Choose an organization from the list:");
        
        dialog.showAndWait().ifPresent(org -> {
            editItem(org);
        });
    }
    
    private void showDeleteDialog() {
        // Show a dialog to select an organization to delete
        if (filteredOrganizations.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Organizations");
            alert.setHeaderText(null);
            alert.setContentText("No organizations found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Organization> orgsToDelete = filteredOrganizations.isEmpty() ? allItems : filteredOrganizations;
        
        // Create a selection dialog
        ChoiceDialog<Organization> dialog = new ChoiceDialog<>(orgsToDelete.get(0), orgsToDelete);
        dialog.setTitle("Delete Organization");
        dialog.setHeaderText("Select an organization to delete");
        dialog.setContentText("Choose an organization from the list:");
        
        dialog.showAndWait().ifPresent(org -> {
            deleteItem(org);
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
    protected List<Organization> loadAllItems() {
        return organizationDAO.findAll();
    }

    @Override
    protected void deleteItem(Organization item) {
        organizationDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Organization newOrg = createNewItem();
        showOrganizationDialog(newOrg, "Add New Organization");
    }

    @Override
    protected void editItem(Organization item) {
        showOrganizationDialog(item, "Edit Organization");
    }

    @Override
    protected Organization createNewItem() {
        Organization org = new Organization();
        org.setCreatedAt(java.time.LocalDateTime.now());
        org.setIsValidated(false);
        return org;
    }

    @Override
    protected void updateItem(Organization item) {
    }

    @Override
    protected boolean saveItem(Organization item) {
        try {
            if (item.getId() == null) {
                organizationDAO.save(item);
            } else {
                organizationDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving organization: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        List<Organization> organizations = loadAllItems();
        filteredOrganizations.clear();
        if (organizations != null) {
            filteredOrganizations.addAll(organizations);
        }
        
        int pageCount = (int) Math.ceil((double) filteredOrganizations.size() / 6);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        countLabel.setText(filteredOrganizations.size() + " organizations");
    }

    protected void filterCards(String searchText) {
        List<Organization> allOrgs = loadAllItems();
        filteredOrganizations.clear();
        
        if (allOrgs != null) {
            for (Organization org : allOrgs) {
                if (org.getName() != null && org.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredOrganizations.add(org);
                }
            }
        }
        
        int pageCount = (int) Math.ceil((double) filteredOrganizations.size() / 6);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
        
        countLabel.setText(filteredOrganizations.size() + " organizations");
    }
    
    private FlowPane createPage(int pageIndex) {
        FlowPane page = new FlowPane(15, 15);
        page.setStyle("-fx-padding: 10;");
        
        int fromIndex = pageIndex * 6;
        int toIndex = Math.min(fromIndex + 6, filteredOrganizations.size());
        
        for (int i = fromIndex; i < toIndex; i++) {
            page.getChildren().add(createOrganizationCard(filteredOrganizations.get(i)));
        }
        
        return page;
    }

    protected VBox createOrganizationCard(Organization org) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Organization logo
        javafx.scene.layout.StackPane logoPane = new javafx.scene.layout.StackPane();
        logoPane.setStyle("-fx-background-color: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); -fx-background-radius: 8;");
        logoPane.setPrefSize(250, 100);
        logoPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label orgIcon = new Label("🏢");
        orgIcon.setStyle("-fx-font-size: 48px;");
        logoPane.getChildren().add(orgIcon);
        
        // Organization name
        Label nameLabel = new Label(org.getName() != null ? org.getName() : "Unknown Organization");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Owner
        String ownerName = "N/A";
        try {
            if (org.getOwner() != null) {
                ownerName = org.getOwner().getUsername();
            }
        } catch (Exception e) {
            ownerName = "N/A";
        }
        Label ownerLabel = new Label("👤 Owner: " + ownerName);
        ownerLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Validated status
        Label validatedLabel = new Label(org.getIsValidated() != null && org.getIsValidated() ? "✅ Validated" : "⏳ Pending Validation");
        validatedLabel.setStyle(org.getIsValidated() != null && org.getIsValidated() ? 
            "-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-font-weight: bold;" : 
            "-fx-text-fill: #F59E0B; -fx-font-size: 12px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(org));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(org));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(logoPane, nameLabel, ownerLabel, validatedLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #38ef7d; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Organization item) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label(item.getId() == null ? "Add New Organization" : "Edit Organization");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        currentNameField = new TextField(item.getName());
        currentNameField.setPromptText("Organization Name");
        currentNameField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentDescriptionField = new TextArea(item.getDescription());
        currentDescriptionField.setPromptText("Description");
        currentDescriptionField.setPrefRowCount(3);
        currentDescriptionField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentLogoField = new TextField(item.getLogo());
        currentLogoField.setPromptText("Logo URL");
        currentLogoField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentOwnerCombo = new ComboBox<>(users);
        currentOwnerCombo.setValue(item.getOwner());
        currentOwnerCombo.setPromptText("Select Owner");
        currentOwnerCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentOwnerCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "Select Owner";
                try {
                    String username = user.getUsername();
                    return username != null ? username : "Owner";
                } catch (Exception e) {
                    return "Owner";
                }
            }
            @Override
            public User fromString(String string) {
                return null;
            }
        });

        currentValidatedCheck = new CheckBox("Is Validated");
        currentValidatedCheck.setSelected(item.getIsValidated() != null ? item.getIsValidated() : false);
        currentValidatedCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label logoLabel = new Label("Logo URL:");
        logoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label ownerLabel = new Label("Owner:");
        ownerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            titleLabel,
            new javafx.scene.layout.Region(),
            nameLabel, currentNameField,
            descriptionLabel, currentDescriptionField,
            logoLabel, currentLogoField,
            ownerLabel, currentOwnerCombo,
            currentValidatedCheck
        );

        return content;
    }

    @Override
    protected boolean validateInput(Organization item) {
        String name = currentNameField == null ? "" : currentNameField.getText().trim();
        String description = currentDescriptionField == null ? "" : currentDescriptionField.getText().trim();
        String logo = currentLogoField == null ? "" : currentLogoField.getText().trim();

        if (name.isEmpty()) {
            showNotImplemented("Organization name is required");
            return false;
        }
        if (name.length() < 4) {
            showNotImplemented("Organization name must be at least 4 characters");
            return false;
        }
        if (name.length() > 10000) {
            showNotImplemented("Organization name must be less than 10000 characters");
            return false;
        }

        if (description.length() > 10000) {
            showNotImplemented("Description must be less than 10000 characters");
            return false;
        }

        if (logo.length() > 10000) {
            showNotImplemented("Logo URL must be less than 10000 characters");
            return false;
        }

        item.setName(name);
        item.setDescription(description);
        item.setLogo(logo);
        item.setOwner(currentOwnerCombo.getValue());
        item.setIsValidated(currentValidatedCheck.isSelected());

        return true;
    }

    private void showOrganizationDialog(Organization org, String title) {
        // Load users before showing dialog to avoid lazy loading errors
        loadUsers();
        
        Dialog<Organization> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style the buttons
        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(org);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(org)) {
                    return org;
                }
            }
            return null;
        });

        Optional<Organization> result = dialog.showAndWait();
        result.ifPresent(savedOrg -> {
            if (saveItem(savedOrg)) {
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
