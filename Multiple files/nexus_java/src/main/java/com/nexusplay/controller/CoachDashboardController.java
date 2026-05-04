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

public class CoachDashboardController implements DashboardController {
    
    private User currentUser;
    
    // DAO instances
    private CoachDAO coachDAO;
    private CoachingSessionDAO coachingSessionDAO;
    private PlayerDAO playerDAO;
    
    @FXML private Label welcomeLabel;
    @FXML private Label coachStatsLabel;
    @FXML private TableView<CoachingSession> upcomingSessionsTable;
    @FXML private TableView<Player> availablePlayersTable;
    @FXML private TabPane coachTabPane;
    
    @FXML
    public void initialize() {
        // Initialize DAOs
        coachDAO = new CoachDAO();
        coachingSessionDAO = new CoachingSessionDAO();
        playerDAO = new PlayerDAO();
        
        // Initialize tables
        initializeSessionsTable();
        initializePlayersTable();
        
        // Load initial data
        loadAvailablePlayers();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
        loadCoachData();
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, Coach " + currentUser.getUsername() + "! 🎬");
        }
    }
    
    private void loadCoachData() {
        if (currentUser != null) {
            try {
                // Find coach associated with this user
                Coach coach = coachDAO.findByUserId(currentUser.getId());
                if (coach != null) {
                    updateCoachStats(coach);
                    loadCoachSessions(coach);
                }
            } catch (Exception e) {
                System.err.println("Error loading coach data: " + e.getMessage());
            }
        }
    }
    
    private void updateCoachStats(Coach coach) {
        StringBuilder stats = new StringBuilder();
        stats.append("🎬 Coach ID: ").append(coach.getId()).append("\n");
        stats.append("� User ID: ").append(coach.getUser() != null ? coach.getUser().getId() : "N/A").append("\n");
        stats.append("⭐ Rating: ").append("5.0/5.0").append("\n");
        stats.append("💰 Price/Session: $50").append("\n");
        stats.append("📚 Experience: Professional").append("\n");
        stats.append("🎮 Specialization: Multiple Games");
        
        coachStatsLabel.setText(stats.toString());
    }
    
    private void loadCoachSessions(Coach coach) {
        try {
            List<CoachingSession> sessions = coachingSessionDAO.findByCoachId(coach.getId());
            ObservableList<CoachingSession> sessionList = FXCollections.observableArrayList(sessions);
            upcomingSessionsTable.setItems(sessionList);
        } catch (Exception e) {
            System.err.println("Error loading coach sessions: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initializeSessionsTable() {
        // Initialize sessions table columns
        TableColumn<CoachingSession, String> playerColumn = new TableColumn<>("Player");
        playerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getPlayer() != null ? cellData.getValue().getPlayer().getNickname() : ""));
        
        TableColumn<CoachingSession, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Scheduled"));
        
        TableColumn<CoachingSession, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Upcoming"));
        
        upcomingSessionsTable.getColumns().addAll((TableColumn<CoachingSession, ?>[]) new TableColumn[]{playerColumn, dateColumn, statusColumn});
    }
    
    @SuppressWarnings("unchecked")
    private void initializePlayersTable() {
        // Initialize players table columns
        TableColumn<Player, String> nicknameColumn = new TableColumn<>("Nickname");
        nicknameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNickname()));
        
        TableColumn<Player, String> gameColumn = new TableColumn<>("Game");
        gameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getGame() != null ? cellData.getValue().getGame().getName() : ""));
        
        TableColumn<Player, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Professional"));
        
        availablePlayersTable.getColumns().addAll((TableColumn<Player, ?>[]) new TableColumn[]{nicknameColumn, gameColumn, levelColumn});
    }
    
    private void loadAvailablePlayers() {
        try {
            List<Player> players = playerDAO.findAll();
            ObservableList<Player> playerList = FXCollections.observableArrayList(players);
            availablePlayersTable.setItems(playerList);
        } catch (Exception e) {
            System.err.println("Error loading players: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleScheduleSession() {
        Player selectedPlayer = availablePlayersTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) {
            showAlert("Please select a player to schedule a session.");
            return;
        }
        
        if (currentUser == null) {
            showAlert("Coach not logged in properly.");
            return;
        }
        
        try {
            // Find coach associated with this user
            Coach coach = coachDAO.findByUserId(currentUser.getId());
            if (coach == null) {
                showAlert("Coach profile not found. Please contact administrator.");
                return;
            }
            
            // Create new coaching session
            CoachingSession session = new CoachingSession();
            session.setCoach(coach);
            session.setPlayer(selectedPlayer);
            session.setScheduledAt(java.time.LocalDateTime.now().plusDays(1)); // Schedule for tomorrow
            session.setStatus("SCHEDULED"); // Use string status instead of enum
            session.setCreatedAt(java.time.LocalDateTime.now());
            
            coachingSessionDAO.save(session);
            
            // Refresh sessions table
            loadCoachSessions(coach);
            
            showAlert("Coaching session scheduled with " + selectedPlayer.getNickname() + " for tomorrow!");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to schedule session: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewProfile() {
        showAlert("Coach profile feature coming soon!");
    }
    
    @FXML
    private void handleManageSchedule() {
        showAlert("Schedule management feature coming soon!");
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
        alert.setTitle("Coach Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
