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




public class VisitorDashboardController implements DashboardController {
    
    private User currentUser;
    
    // DAO instances
    private GameDAO gameDAO;
    private TeamDAO teamDAO;
    
    @FXML private Label welcomeLabel;
    @FXML private TableView<Game> featuredGamesTable;
    @FXML private TableView<Team> topTeamsTable;
    @FXML private TabPane visitorTabPane;
    
    @FXML
    public void initialize() {
        // Initialize DAOs
        gameDAO = new GameDAO();
        teamDAO = new TeamDAO();
        
        // Initialize tables
        initializeGamesTable();
        initializeTeamsTable();
        
        // Load initial data
        loadFeaturedGames();
        loadTopTeams();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome to NexusPlay, " + currentUser.getUsername() + "! 🎮");
        } else {
            welcomeLabel.setText("Welcome to NexusPlay! 🎮");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initializeGamesTable() {
        // Initialize games table columns
        TableColumn<Game, String> nameColumn = new TableColumn<>("Game Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<Game, Integer> yearColumn = new TableColumn<>("Release Year");
        yearColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReleaseYear()).asObject());
        
        TableColumn<Game, String> genreColumn = new TableColumn<>("Genre");
        genreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Competitive"));
        
        featuredGamesTable.getColumns().addAll((TableColumn<Game, ?>[]) new TableColumn[]{nameColumn, yearColumn, genreColumn});
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
        
        topTeamsTable.getColumns().addAll((TableColumn<Team, ?>[]) new TableColumn[]{nameColumn, gameColumn, countryColumn});
    }
    
    private void loadFeaturedGames() {
        try {
            List<Game> games = gameDAO.findAll();
            ObservableList<Game> gameList = FXCollections.observableArrayList(games);
            featuredGamesTable.setItems(gameList);
        } catch (Exception e) {
            System.err.println("Error loading games: " + e.getMessage());
        }
    }
    
    private void loadTopTeams() {
        try {
            List<Team> teams = teamDAO.findAll();
            ObservableList<Team> teamList = FXCollections.observableArrayList(teams);
            topTeamsTable.setItems(teamList);
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSignUp() {
        showAlert("Please logout and click 'Create New Account' to sign up as a player!");
    }
    
    @FXML
    private void handleBrowseGames() {
        showAlert("Game browsing feature - explore our competitive gaming titles!");
    }
    
    @FXML
    private void handleViewTournaments() {
        showAlert("Tournament viewing feature - watch live matches and results!");
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
        alert.setTitle("Visitor Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
