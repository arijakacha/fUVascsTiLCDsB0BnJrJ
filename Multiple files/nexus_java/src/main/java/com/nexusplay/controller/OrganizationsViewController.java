package com.nexusplay.controller;

import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;
import com.nexusplay.util.FlowLayoutHelper;
import com.nexusplay.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OrganizationsViewController extends BaseController {




    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label avatarLabel;
    @FXML private VBox mainContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane organizationsFlowPane;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button previousPageButton;
    @FXML private Button nextPageButton;
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

    private int currentPage = 1;
    private final int pageSize = 6;
    private String searchQuery = "";

    @FXML
    public void initialize() {
        bindWindowChrome();
        FlowLayoutHelper.bindPrefWrapLengthToRegion(mainContent, organizationsFlowPane, 80);
        loadUserInfo();
        loadOrganizations();
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

    private void loadOrganizations() {
        System.out.println("OrganizationsViewController: Loading organizations page " + currentPage + " with search: '" + searchQuery + "'");
        organizationsFlowPane.getChildren().clear();
        UserDAO userDAO = new UserDAO();
        
        java.util.List<User> organizations;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            organizations = userDAO.findAll();
        } else {
            organizations = searchOrganizations(userDAO, searchQuery);
        }
        
        // Filter only ORGANIZATION type users
        java.util.List<User> organizationUsers = new java.util.ArrayList<>();
        for (User user : organizations) {
            if (user.getUserType() == User.UserType.ORGANIZATION) {
                organizationUsers.add(user);
            }
        }
        
        System.out.println("OrganizationsViewController: Found " + (organizationUsers != null ? organizationUsers.size() : "null") + " organizations");
        
        if (organizationUsers != null && !organizationUsers.isEmpty()) {
            for (User organization : organizationUsers) {
                organizationsFlowPane.getChildren().add(createOrganizationCard(organization));
            }
        } else {
            Label noOrganizationsLabel = new Label("No organizations available");
            noOrganizationsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 16px;");
            organizationsFlowPane.getChildren().add(noOrganizationsLabel);
        }
        
        updatePagination();
    }
    
    private java.util.List<User> searchOrganizations(UserDAO userDAO, String query) {
        try {
            java.util.List<User> allUsers = userDAO.findAll();
            java.util.List<User> filtered = new java.util.ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (User user : allUsers) {
                boolean matchesUsername = user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerQuery);
                boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                
                if (matchesUsername || matchesEmail) {
                    filtered.add(user);
                }
            }
            
            // Apply pagination to filtered results
            int startIndex = (currentPage - 1) * pageSize;
            if (startIndex >= filtered.size()) {
                return new java.util.ArrayList<>();
            }
            int endIndex = Math.min(startIndex + pageSize, filtered.size());
            return filtered.subList(startIndex, endIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private VBox createOrganizationCard(User organization) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1A1D26; -fx-background-radius: 12; -fx-padding: 20; -fx-spacing: 16;");
        card.setPrefWidth(400);
        
        // Username
        Label usernameLabel = new Label(organization.getUsername());
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        usernameLabel.setWrapText(true);
        card.getChildren().add(usernameLabel);
        
        // Email
        Label emailLabel = new Label(organization.getEmail());
        emailLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        emailLabel.setWrapText(true);
        card.getChildren().add(emailLabel);
        
        // User type badge
        Label typeBadge = new Label(organization.getUserType() != null ? organization.getUserType().toString() : "ORGANIZATION");
        typeBadge.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-background-color: #7C3AED;");
        card.getChildren().add(typeBadge);
        
        // Status badge
        Label statusBadge = new Label(organization.getStatus() != null ? organization.getStatus().toString() : "ACTIVE");
        statusBadge.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-background-color: " + getStatusColor(organization.getStatus()) + ";");
        card.getChildren().add(statusBadge);
        
        // Join date
        Label dateLabel = new Label("Joined: " + formatDate(organization.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.40); -fx-font-size: 12px;");
        card.getChildren().add(dateLabel);
        
        return card;
    }

    private String getStatusColor(User.UserStatus status) {
        if (status == null) return "#6B7280";
        switch (status) {
            case ACTIVE: return "#10B981";
            case BANNED: return "#E74C3C";
            default: return "#6B7280";
        }
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private void updatePagination() {
        UserDAO userDAO = new UserDAO();
        long totalOrganizations;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            java.util.List<User> allUsers = userDAO.findAll();
            java.util.List<User> organizationUsers = new java.util.ArrayList<>();
            for (User user : allUsers) {
                if (user.getUserType() == User.UserType.ORGANIZATION) {
                    organizationUsers.add(user);
                }
            }
            totalOrganizations = organizationUsers.size();
        } else {
            java.util.List<User> allUsers = userDAO.findAll();
            java.util.List<User> filtered = new java.util.ArrayList<>();
            String lowerQuery = searchQuery.toLowerCase();
            
            for (User user : allUsers) {
                boolean matchesUsername = user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerQuery);
                boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                
                if (matchesUsername || matchesEmail) {
                    filtered.add(user);
                }
            }
            
            java.util.List<User> organizationUsers = new java.util.ArrayList<>();
            for (User user : filtered) {
                if (user.getUserType() == User.UserType.ORGANIZATION) {
                    organizationUsers.add(user);
                }
            }
            totalOrganizations = organizationUsers.size();
        }
        
        int totalPages = (int) Math.ceil((double) totalOrganizations / pageSize);
        
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        previousPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages || totalPages == 0);
    }

    @FXML
    private void handleSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            searchQuery = searchField.getText().trim();
            currentPage = 1;
            loadOrganizations();
        }
    }

    @FXML
    private void handleSearch() {
        searchQuery = searchField.getText().trim();
        currentPage = 1;
        loadOrganizations();
    }

    @FXML
    private void handleNextPage() {
        try {
            UserDAO userDAO = new UserDAO();
            long totalOrganizations;
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                java.util.List<User> allUsers = userDAO.findAll();
                java.util.List<User> organizationUsers = new java.util.ArrayList<>();
                for (User user : allUsers) {
                    if (user.getUserType() == User.UserType.ORGANIZATION) {
                        organizationUsers.add(user);
                    }
                }
                totalOrganizations = organizationUsers.size();
            } else {
                java.util.List<User> allUsers = userDAO.findAll();
                java.util.List<User> filtered = new java.util.ArrayList<>();
                String lowerQuery = searchQuery.toLowerCase();
                
                for (User user : allUsers) {
                    boolean matchesUsername = user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerQuery);
                    boolean matchesEmail = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                    
                    if (matchesUsername || matchesEmail) {
                        filtered.add(user);
                    }
                }
                
                java.util.List<User> organizationUsers = new java.util.ArrayList<>();
                for (User user : filtered) {
                    if (user.getUserType() == User.UserType.ORGANIZATION) {
                        organizationUsers.add(user);
                    }
                }
                totalOrganizations = organizationUsers.size();
            }
            int totalPages = (int) Math.ceil((double) totalOrganizations / pageSize);
            if (currentPage < totalPages) {
                currentPage++;
                loadOrganizations();
            }
        } catch (Exception e) {
            System.err.println("Error in handleNextPage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePreviousPage() {
        try {
            if (currentPage > 1) {
                currentPage--;
                loadOrganizations();
            }
        } catch (Exception e) {
            System.err.println("Error in handlePreviousPage: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ShopView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // Reload organizations when already on organizations page
        System.out.println("OrganizationsViewController: showOrganizations() called");
        loadOrganizations();
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
