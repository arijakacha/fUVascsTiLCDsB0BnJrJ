package com.nexusplay.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.nexusplay.dao.*;
import com.nexusplay.entity.*;
import com.nexusplay.util.SceneNavigation;
import com.nexusplay.util.SessionManager;

import java.util.List;

public class PlayerDashboardController implements DashboardController {
    
    private User currentUser;
    
    // DAO instances
    private PlayerDAO playerDAO;
    private TeamDAO teamDAO;
    private GameDAO gameDAO;
    
    @FXML private Label welcomeLabel;
    @FXML private Label playerStatsLabel;
    @FXML private TableView<Game> availableGamesTable;
    @FXML private TableView<Team> availableTeamsTable;
    @FXML private TabPane playerTabPane;
    
    @FXML
    public void initialize() {
        // Initialize DAOs
        playerDAO = new PlayerDAO();
        teamDAO = new TeamDAO();
        gameDAO = new GameDAO();
        
        // Initialize tables
        initializeGamesTable();
        initializeTeamsTable();
        
        // Load initial data
        loadAvailableGames();
        loadAvailableTeams();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
        loadPlayerData();
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getUsername() + "! 🎮");
        }
    }
    
    private void loadPlayerData() {
        if (currentUser != null) {
            // Load player-specific data
            try {
                // Find player associated with this user
                List<Player> players = playerDAO.findAll();
                for (Player player : players) {
                    if (player.getUser() != null && player.getUser().getId().equals(currentUser.getId())) {
                        updatePlayerStats(player);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading player data: " + e.getMessage());
            }
        }
    }
    
    private void updatePlayerStats(Player player) {
        StringBuilder stats = new StringBuilder();
        stats.append("🎯 Player ID: ").append(player.getId()).append("\n");
        stats.append("👤 Nickname: ").append(player.getNickname()).append("\n");
        stats.append("� Game: ").append(player.getGame() != null ? player.getGame().getName() : "Not Assigned").append("\n");
        stats.append("⚔️ Team: ").append(player.getTeam() != null ? player.getTeam().getName() : "No Team");
        
        playerStatsLabel.setText(stats.toString());
    }
    
    @SuppressWarnings("unchecked")
    private void initializeGamesTable() {
        // Initialize game table columns
        TableColumn<Game, String> nameColumn = new TableColumn<>("Game Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<Game, Integer> yearColumn = new TableColumn<>("Release Year");
        yearColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReleaseYear()).asObject());
        
        availableGamesTable.getColumns().addAll((TableColumn<Game, ?>[]) new TableColumn[]{nameColumn, yearColumn});
    }
    
    @SuppressWarnings("unchecked")
    private void initializeTeamsTable() {
        // Initialize team table columns
        TableColumn<Team, String> nameColumn = new TableColumn<>("Team Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<Team, String> gameColumn = new TableColumn<>("Game");
        gameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getGame() != null ? cellData.getValue().getGame().getName() : ""));
        
        TableColumn<Team, String> countryColumn = new TableColumn<>("Country");
        countryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCountry()));
        
        availableTeamsTable.getColumns().addAll((TableColumn<Team, ?>[]) new TableColumn[]{nameColumn, gameColumn, countryColumn});
    }
    
    private void loadAvailableGames() {
        try {
            List<Game> games = gameDAO.findAll();
            ObservableList<Game> gameList = FXCollections.observableArrayList(games);
            availableGamesTable.setItems(gameList);
        } catch (Exception e) {
            System.err.println("Error loading games: " + e.getMessage());
        }
    }
    
    private void loadAvailableTeams() {
        try {
            List<Team> teams = teamDAO.findAll();
            ObservableList<Team> teamList = FXCollections.observableArrayList(teams);
            availableTeamsTable.setItems(teamList);
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleJoinTeam() {
        Team selectedTeam = availableTeamsTable.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showAlert("Please select a team to join.");
            return;
        }
        
        if (currentUser == null) {
            showAlert("User not logged in properly.");
            return;
        }
        
        try {
            // Check if player already exists for this user
            List<Player> existingPlayers = playerDAO.findAll();
            Player player = null;
            
            for (Player p : existingPlayers) {
                if (p.getUser() != null && p.getUser().getId().equals(currentUser.getId())) {
                    player = p;
                    break;
                }
            }
            
            if (player == null) {
                // Create new player record
                player = new Player();
                player.setUser(currentUser);
                player.setNickname(currentUser.getUsername());
                player.setGame(selectedTeam.getGame());
                player.setTeam(selectedTeam);
                player.setCreatedAt(java.time.LocalDateTime.now());
                player.setScore(0);
                player.setIsPro(false);
                player.setSmsConsent(false);
                
                playerDAO.save(player);
                showAlert("Successfully joined team '" + selectedTeam.getName() + "' as new player!");
            } else {
                // Update existing player's team
                player.setTeam(selectedTeam);
                player.setGame(selectedTeam.getGame()); // Update game to match team's game
                playerDAO.update(player);
                showAlert("Successfully switched to team '" + selectedTeam.getName() + "'!");
            }
            
            // Refresh player stats
            loadPlayerData();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to join team: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewProfile() {
        showAlert("Player profile feature coming soon!");
    }
    
    @FXML
    private void handleViewAchievements() {
        showAlert("Achievements feature coming soon!");
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
        alert.setTitle("Player Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
