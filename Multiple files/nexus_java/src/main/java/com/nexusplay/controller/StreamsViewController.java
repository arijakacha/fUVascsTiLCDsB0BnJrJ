package com.nexusplay.controller;

import com.nexusplay.dao.LikeDAO;
import com.nexusplay.dao.StreamDAO;
import com.nexusplay.entity.Stream;
import com.nexusplay.util.FlowLayoutHelper;
import com.nexusplay.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;




public class StreamsViewController extends BaseController {

    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label avatarLabel;
    @FXML private VBox mainContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane streamsFlowPane;
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
        FlowLayoutHelper.bindPrefWrapLengthToRegion(mainContent, streamsFlowPane, 80);
        loadUserInfo();
        loadStreams();
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

    private void loadStreams() {
        System.out.println("StreamsViewController: Loading streams page " + currentPage + " with search: '" + searchQuery + "'");
        streamsFlowPane.getChildren().clear();
        StreamDAO streamDAO = new StreamDAO();
        
        java.util.List<Stream> streams;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            streams = streamDAO.findAllPaginated(currentPage - 1, pageSize);
        } else {
            streams = searchStreams(streamDAO, searchQuery);
        }
        
        System.out.println("StreamsViewController: Found " + (streams != null ? streams.size() : "null") + " streams");
        
        if (streams != null && !streams.isEmpty()) {
            for (Stream stream : streams) {
                streamsFlowPane.getChildren().add(createStreamCard(stream));
            }
        } else {
            Label noStreamsLabel = new Label("No streams available");
            noStreamsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 16px;");
            streamsFlowPane.getChildren().add(noStreamsLabel);
        }
        
        updatePagination();
    }
    
    private java.util.List<Stream> searchStreams(StreamDAO streamDAO, String query) {
        try {
            java.util.List<Stream> allStreams = streamDAO.findAll();
            java.util.List<Stream> filtered = new java.util.ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (Stream stream : allStreams) {
                boolean matchesTitle = stream.getTitle() != null && stream.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesStreamer = stream.getPlayer() != null && stream.getPlayer().getNickname() != null 
                    && stream.getPlayer().getNickname().toLowerCase().contains(lowerQuery);
                
                if (matchesTitle || matchesStreamer) {
                    filtered.add(stream);
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

    private VBox createStreamCard(Stream stream) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1A1D26; -fx-background-radius: 12; -fx-padding: 20; -fx-spacing: 16;");
        card.setPrefWidth(400);
        
        // Stream title
        Label titleLabel = new Label(stream.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        card.getChildren().add(titleLabel);
        
        // Streamer name
        Label streamerLabel = new Label("Streamer: " + (stream.getPlayer() != null ? stream.getPlayer().getNickname() : "Unknown"));
        streamerLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        card.getChildren().add(streamerLabel);
        
        // Live status
        Label statusLabel = new Label(stream.getIsLive() ? "🔴 LIVE" : "OFFLINE");
        statusLabel.setStyle(stream.getIsLive() 
            ? "-fx-text-fill: #FF0000; -fx-font-size: 14px; -fx-font-weight: bold;" 
            : "-fx-text-fill: rgba(255,255,255,0.40); -fx-font-size: 14px;");
        card.getChildren().add(statusLabel);
        
        // Stream date
        Label dateLabel = new Label(formatDate(stream.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.40); -fx-font-size: 12px;");
        card.getChildren().add(dateLabel);
        
        // Like/Dislike section
        HBox actionBox = new HBox(10);

        LikeDAO countsDao = new LikeDAO();
        long initialLikes = countsDao.countLikesByStream(stream.getId());
        long initialDislikes = countsDao.countDislikesByStream(stream.getId());
        
        final int streamId = stream.getId();

        Button likeButton = new Button("👍 " + initialLikes);
        likeButton.setStyle("-fx-background-color: #2D7A3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");

        Button dislikeButton = new Button("👎 " + initialDislikes);
        dislikeButton.setStyle("-fx-background-color: #A04030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");

        likeButton.setOnAction(e -> {
            System.out.println("Like button clicked for stream " + streamId);
            new Thread(() -> {
                try {
                    var currentUser = SessionManager.getCurrentUser();
                    if (currentUser == null) {
                        System.err.println("User not logged in");
                        return;
                    }

                    LikeDAO likeDAO = new LikeDAO();
                    likeDAO.toggleStreamReactionByIdsJdbc(currentUser.getId(), streamId, "like");
                    System.out.println("Like saved successfully");

                    Platform.runLater(() -> {
                        try {
                            long likes = likeDAO.countLikesByStream(streamId);
                            long dislikes = likeDAO.countDislikesByStream(streamId);
                            likeButton.setText("👍 " + likes);
                            dislikeButton.setText("👎 " + dislikes);
                        } catch (Exception uiEx) {
                            System.err.println("Error updating like UI: " + uiEx.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("Error liking: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }, "stream-like-" + streamId).start();
        });

        dislikeButton.setOnAction(e -> {
            System.out.println("Dislike button clicked for stream " + streamId);
            new Thread(() -> {
                try {
                    var currentUser = SessionManager.getCurrentUser();
                    if (currentUser == null) {
                        System.err.println("User not logged in");
                        return;
                    }

                    LikeDAO likeDAO = new LikeDAO();
                    likeDAO.toggleStreamReactionByIdsJdbc(currentUser.getId(), streamId, "dislike");
                    System.out.println("Dislike saved successfully");

                    Platform.runLater(() -> {
                        try {
                            long likes = likeDAO.countLikesByStream(streamId);
                            long dislikes = likeDAO.countDislikesByStream(streamId);
                            likeButton.setText("👍 " + likes);
                            dislikeButton.setText("👎 " + dislikes);
                        } catch (Exception uiEx) {
                            System.err.println("Error updating dislike UI: " + uiEx.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("Error disliking: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }, "stream-dislike-" + streamId).start();
        });
        
        actionBox.getChildren().addAll(likeButton, dislikeButton);
        card.getChildren().add(actionBox);
        
        // Watch button
        Button watchButton = new Button("Watch Stream");
        watchButton.setStyle("-fx-background-color: #3D2FA0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-size: 13px;");
        watchButton.setOnAction(e -> watchStream(stream));
        
        card.getChildren().add(watchButton);
        
        return card;
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private void watchStream(Stream stream) {
        if (stream.getUrl() != null && !stream.getUrl().isEmpty()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(stream.getUrl()));
            } catch (Exception e) {
                System.err.println("Error opening stream URL: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Stream URL is empty");
        }
    }

    private void updatePagination() {
        StreamDAO streamDAO = new StreamDAO();
        long totalStreams;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            totalStreams = streamDAO.countAll();
        } else {
            java.util.List<Stream> allStreams = streamDAO.findAll();
            java.util.List<Stream> filtered = new java.util.ArrayList<>();
            String lowerQuery = searchQuery.toLowerCase();
            
            for (Stream stream : allStreams) {
                boolean matchesTitle = stream.getTitle() != null && stream.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesStreamer = stream.getPlayer() != null && stream.getPlayer().getNickname() != null 
                    && stream.getPlayer().getNickname().toLowerCase().contains(lowerQuery);
                
                if (matchesTitle || matchesStreamer) {
                    filtered.add(stream);
                }
            }
            totalStreams = filtered.size();
        }
        
        int totalPages = (int) Math.ceil((double) totalStreams / pageSize);
        
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        previousPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages || totalPages == 0);
    }

    @FXML
    private void handleSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            searchQuery = searchField.getText().trim();
            currentPage = 1;
            loadStreams();
        }
    }

    @FXML
    private void handleSearch() {
        searchQuery = searchField.getText().trim();
        currentPage = 1;
        loadStreams();
    }

    @FXML
    private void handleNextPage() {
        try {
            StreamDAO streamDAO = new StreamDAO();
            long totalStreams;
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                totalStreams = streamDAO.countAll();
            } else {
                java.util.List<Stream> allStreams = streamDAO.findAll();
                java.util.List<Stream> filtered = new java.util.ArrayList<>();
                String lowerQuery = searchQuery.toLowerCase();
                
                for (Stream stream : allStreams) {
                    boolean matchesTitle = stream.getTitle() != null && stream.getTitle().toLowerCase().contains(lowerQuery);
                    boolean matchesStreamer = stream.getPlayer() != null && stream.getPlayer().getNickname() != null 
                        && stream.getPlayer().getNickname().toLowerCase().contains(lowerQuery);
                    
                    if (matchesTitle || matchesStreamer) {
                        filtered.add(stream);
                    }
                }
                totalStreams = filtered.size();
            }
            int totalPages = (int) Math.ceil((double) totalStreams / pageSize);
            if (currentPage < totalPages) {
                currentPage++;
                loadStreams();
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
                loadStreams();
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
        // Reload streams when already on streams page
        System.out.println("StreamsViewController: showStreams() called");
        loadStreams();
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/OrganizationsView.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
