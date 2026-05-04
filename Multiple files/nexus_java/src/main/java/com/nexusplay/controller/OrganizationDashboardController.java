package com.nexusplay.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import com.nexusplay.dao.*;
import com.nexusplay.entity.*;
import com.nexusplay.util.SceneNavigation;
import com.nexusplay.util.SessionManager;

import java.util.List;
import java.util.Optional;

public class OrganizationDashboardController implements DashboardController {
    
   
   
    private User currentUser;
    
    // DAO instances
    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private GameDAO gameDAO;
    
    @FXML private Label welcomeLabel;
    @FXML private Label orgStatsLabel;
    @FXML private TableView<Team> organizationTeamsTable;
    @FXML private TableView<Player> organizationPlayersTable;
    @FXML private TabPane orgTabPane;
    
    @FXML
    public void initialize() {
        // Initialize DAOs
        teamDAO = new TeamDAO();
        playerDAO = new PlayerDAO();
        gameDAO = new GameDAO();
        
        // Initialize tables
        initializeTeamsTable();
        initializePlayersTable();
        
        // Load initial data
        loadOrganizationTeams();
        loadOrganizationPlayers();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
        loadOrganizationData();
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getUsername() + "! 🏢");
        }
    }
    
    private void loadOrganizationData() {
        if (currentUser != null) {
            updateOrganizationStats();
        }
    }
    
    private void updateOrganizationStats() {
        StringBuilder stats = new StringBuilder();
        try {
            int teamCount = teamDAO.findAll().size();
            int playerCount = playerDAO.findAll().size();
            int gameCount = gameDAO.findAll().size();
            
            stats.append("🏆 Teams: ").append(teamCount).append("\n");
            stats.append("👥 Players: ").append(playerCount).append("\n");
            stats.append("🎮 Games: ").append(gameCount).append("\n");
            stats.append("📊 Organization Level: Professional");
            
        } catch (Exception e) {
            stats.append("Error loading statistics");
        }
        
        orgStatsLabel.setText(stats.toString());
    }
    
    @SuppressWarnings("unchecked")
    private void initializeTeamsTable() {
        // Initialize teams table columns
        TableColumn<Team, String> nameColumn = new TableColumn<>("Team Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<Team, String> gameColumn = new TableColumn<>("Game");
        gameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getGame() != null ? cellData.getValue().getGame().getName() : ""));
        
        TableColumn<Team, String> countryColumn = new TableColumn<>("Country");
        countryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCountry()));
        
        organizationTeamsTable.getColumns().addAll((TableColumn<Team, ?>[]) new TableColumn[]{nameColumn, gameColumn, countryColumn});
    }
    
    @SuppressWarnings("unchecked")
    private void initializePlayersTable() {
        // Initialize players table columns
        TableColumn<Player, String> nicknameColumn = new TableColumn<>("Nickname");
        nicknameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNickname()));
        
        TableColumn<Player, String> gameColumn = new TableColumn<>("Game");
        gameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getGame() != null ? cellData.getValue().getGame().getName() : ""));
        
        TableColumn<Player, String> teamColumn = new TableColumn<>("Team");
        teamColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getTeam() != null ? cellData.getValue().getTeam().getName() : "No Team"));
        
        organizationPlayersTable.getColumns().addAll((TableColumn<Player, ?>[]) new TableColumn[]{nicknameColumn, gameColumn, teamColumn});
    }
    
    private void loadOrganizationTeams() {
        try {
            List<Team> teams = teamDAO.findAll();
            ObservableList<Team> teamList = FXCollections.observableArrayList(teams);
            organizationTeamsTable.setItems(teamList);
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
        }
    }
    
    private void loadOrganizationPlayers() {
        try {
            List<Player> players = playerDAO.findAll();
            ObservableList<Player> playerList = FXCollections.observableArrayList(players);
            organizationPlayersTable.setItems(playerList);
        } catch (Exception e) {
            System.err.println("Error loading players: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateTeam() {
        // Get available games for selection
        List<Game> games = gameDAO.findAll();
        if (games == null || games.isEmpty()) {
            showAlert("No games available. Please add games first.");
            return;
        }

        // Create dialog for team name
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Create Team");
        nameDialog.setHeaderText("Create New Team");
        nameDialog.setContentText("Team Name:");

        Optional<String> teamNameResult = nameDialog.showAndWait();
        if (teamNameResult.isEmpty() || teamNameResult.get().trim().isEmpty()) {
            showAlert("Team name is required.");
            return;
        }

        String teamName = teamNameResult.get().trim();
        if (teamName.length() < 4) {
            showAlert("Team name must be at least 4 characters.");
            return;
        }
        if (teamName.length() > 10000) {
            showAlert("Team name must be less than 10000 characters.");
            return;
        }

        // Create dialog for game selection
        ChoiceDialog<Game> gameDialog = new ChoiceDialog<>(games.get(0), games);
        gameDialog.setTitle("Select Game");
        gameDialog.setHeaderText("Select Game for Team");
        gameDialog.setContentText("Choose a game:");

        Optional<Game> gameResult = gameDialog.showAndWait();
        if (gameResult.isEmpty()) {
            return;
        }

        // Create dialog for country (optional)
        TextInputDialog countryDialog = new TextInputDialog();
        countryDialog.setTitle("Team Country");
        countryDialog.setHeaderText("Enter Country (Optional)");
        countryDialog.setContentText("Country:");

        Optional<String> countryResult = countryDialog.showAndWait();

        String country = "";
        if (countryResult.isPresent() && !countryResult.get().trim().isEmpty()) {
            country = countryResult.get().trim();
            if (country.length() > 10000) {
                showAlert("Country must be less than 10000 characters.");
                return;
            }
        }
        
        try {
            // Create new team
            Team newTeam = new Team();
            newTeam.setName(teamName);
            newTeam.setGame(gameResult.get());
            newTeam.setCountry(country);
            newTeam.setCreatedAt(java.time.LocalDateTime.now());
            
            // Link to organization if current user represents an organization
            if (currentUser != null) {
                // For now, we'll assume the organization is linked via user
                // In a real implementation, you might have an Organization entity
            }
            
            // Save team to database
            teamDAO.save(newTeam);
            
            // Refresh teams table
            loadOrganizationTeams();
            
            showAlert("Team '" + newTeam.getName() + "' created successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to create team: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleManageTournaments() {
        showAlert("Tournament management feature coming soon!");
    }
    
    @FXML
    private void handleViewProfile() {
        showAlert("Organization profile feature coming soon!");
    }

    @FXML
    private void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, loginRoot, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
            stage.setMaximized(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Organization Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
