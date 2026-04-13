package com.nexusplay.controller.crud;

import com.nexusplay.dao.TeamDAO;
import com.nexusplay.dao.GameDAO;
import com.nexusplay.entity.Team;
import com.nexusplay.entity.Game;
import com.nexusplay.entity.Organization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class TeamsCRUDController extends CRUDController<Team> {

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

    private TeamDAO teamDAO = new TeamDAO();
    private GameDAO gameDAO = new GameDAO();
    private ObservableList<Game> games = FXCollections.observableArrayList();
    private ObservableList<Organization> organizations = FXCollections.observableArrayList();
    
    private TextField currentNameField;
    private TextField currentCountryField;
    private TextField currentFoundationYearField;
    private ComboBox<Game> currentGameCombo;
    private ComboBox<Organization> currentOrgCombo;
    private TextArea currentDescriptionField;
    private TextField currentLogoField;

    @FXML
    public void initialize() {
        loadGames();
        loadOrganizations();
        setupButtons();
    }

    protected void loadGames() {
        games.addAll(gameDAO.findAll());
    }

    protected void loadOrganizations() {
        organizations.clear(); // Will implement when OrganizationDAO is available
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Team newTeam = createNewItem();
            showTeamDialog(newTeam, "Add New Team");
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
    protected List<Team> loadAllItems() {
        return teamDAO.findAll();
    }

    @Override
    protected void deleteItem(Team item) {
        teamDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Team newTeam = createNewItem();
        showTeamDialog(newTeam, "Add New Team");
    }

    @Override
    protected void editItem(Team item) {
        showTeamDialog(item, "Edit Team");
    }

    @Override
    protected Team createNewItem() {
        Team team = new Team();
        team.setCreatedAt(java.time.LocalDateTime.now());
        return team;
    }

    @Override
    protected void updateItem(Team item) {
    }

    @Override
    protected boolean saveItem(Team item) {
        try {
            if (item.getId() == null) {
                teamDAO.save(item);
            } else {
                teamDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving team: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        List<Team> teams = loadAllItems();
        cardsFlowPane.getChildren().clear();
        
        for (Team team : teams) {
            cardsFlowPane.getChildren().add(createTeamCard(team));
        }
        
        countLabel.setText(teams.size() + " teams");
    }

    protected void filterCards(String searchText) {
        List<Team> allTeams = loadAllItems();
        cardsFlowPane.getChildren().clear();
        
        for (Team team : allTeams) {
            if (team.getName() != null && team.getName().toLowerCase().contains(searchText.toLowerCase())) {
                cardsFlowPane.getChildren().add(createTeamCard(team));
            }
        }
        
        countLabel.setText(cardsFlowPane.getChildren().size() + " teams");
    }

    protected VBox createTeamCard(Team team) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Team icon
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setStyle("-fx-background-color: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); -fx-background-radius: 8;");
        iconPane.setPrefSize(250, 100);
        iconPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label teamIcon = new Label("🏟️");
        teamIcon.setStyle("-fx-font-size: 48px;");
        iconPane.getChildren().add(teamIcon);
        
        // Team name
        Label nameLabel = new Label(team.getName() != null && team.getName().length() > 30 ? 
            team.getName().substring(0, 30) + "..." : team.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Country
        Label countryLabel = new Label(team.getCountry() != null ? "🌍 " + team.getCountry() : "🌍 Country: N/A");
        countryLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Game
        Label gameLabel = new Label(team.getGame() != null ? "🎮 " + team.getGame().getName() : "🎮 Game: N/A");
        gameLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Foundation year
        Label yearLabel = new Label(team.getFoundationYear() != null ? "📅 Founded: " + team.getFoundationYear() : "📅 Founded: N/A");
        yearLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(team));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(team));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(iconPane, nameLabel, countryLabel, gameLabel, yearLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #4facfe; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Team item) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        currentNameField = new TextField(item.getName());
        currentNameField.setPromptText("Team Name");

        currentCountryField = new TextField(item.getCountry());
        currentCountryField.setPromptText("Country");

        currentFoundationYearField = new TextField(item.getFoundationYear() != null ? item.getFoundationYear().toString() : "");
        currentFoundationYearField.setPromptText("Foundation Year");

        currentGameCombo = new ComboBox<>(games);
        currentGameCombo.setValue(item.getGame());
        currentGameCombo.setPromptText("Select Game");

        currentOrgCombo = new ComboBox<>(organizations);
        currentOrgCombo.setValue(item.getOrganization());
        currentOrgCombo.setPromptText("Select Organization (Optional)");

        currentDescriptionField = new TextArea(item.getDescription());
        currentDescriptionField.setPromptText("Description");
        currentDescriptionField.setPrefRowCount(3);

        currentLogoField = new TextField(item.getLogo());
        currentLogoField.setPromptText("Logo URL");

        content.getChildren().addAll(
            new Label("Name:"), currentNameField,
            new Label("Country:"), currentCountryField,
            new Label("Foundation Year:"), currentFoundationYearField,
            new Label("Game:"), currentGameCombo,
            new Label("Organization:"), currentOrgCombo,
            new Label("Description:"), currentDescriptionField,
            new Label("Logo URL:"), currentLogoField
        );

        return content;
    }

    @Override
    protected boolean validateInput(Team item) {
        String name = currentNameField == null ? "" : currentNameField.getText().trim();
        String country = currentCountryField == null ? "" : currentCountryField.getText().trim();
        String yearStr = currentFoundationYearField == null ? "" : currentFoundationYearField.getText().trim();
        String description = currentDescriptionField == null ? "" : currentDescriptionField.getText().trim();
        String logo = currentLogoField == null ? "" : currentLogoField.getText().trim();

        if (name.isEmpty()) {
            showNotImplemented("Team name is required");
            return false;
        }
        if (name.length() < 4) {
            showNotImplemented("Team name must be at least 4 characters");
            return false;
        }
        if (name.length() > 10000) {
            showNotImplemented("Team name must be less than 10000 characters");
            return false;
        }

        if (country.length() > 10000) {
            showNotImplemented("Country must be less than 10000 characters");
            return false;
        }

        if (yearStr.length() > 10000) {
            showNotImplemented("Foundation year must be less than 10000 characters");
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

        if (currentGameCombo.getValue() == null) {
            showNotImplemented("Game selection is required");
            return false;
        }

        try {
            item.setName(name);
            item.setCountry(country);
            
            if (!yearStr.isEmpty()) {
                item.setFoundationYear(Short.parseShort(yearStr));
            }
            
            item.setGame(currentGameCombo.getValue());
            item.setOrganization(currentOrgCombo.getValue());
            item.setDescription(description);
            item.setLogo(logo);
        } catch (NumberFormatException e) {
            showNotImplemented("Foundation year must be a valid number");
            return false;
        }

        return true;
    }

    private void showTeamDialog(Team team, String title) {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        VBox content = createDialogContent(team);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(team)) {
                    return team;
                }
            }
            return null;
        });

        Optional<Team> result = dialog.showAndWait();
        result.ifPresent(savedTeam -> {
            if (saveItem(savedTeam)) {
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
