package com.nexusplay.controller;

import com.nexusplay.dao.ContentDAO;
import com.nexusplay.dao.GuideCommentDAO;
import com.nexusplay.dao.LikeDAO;
import com.nexusplay.entity.Content;
import com.nexusplay.entity.GuideComment;
import com.nexusplay.util.FlowLayoutHelper;
import com.nexusplay.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;




public class ContentViewController extends BaseController {












    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label avatarLabel;
    @FXML private VBox mainContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane contentFlowPane;
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
    @FXML private Label pageLabel;
    @FXML private Button previousPageButton;
    @FXML private Button nextPageButton;

    private int currentPage = 1;
    private final int pageSize = 6;

    @FXML
    public void initialize() {
        System.out.println("ContentViewController: initialize() called");
        bindWindowChrome();
        FlowLayoutHelper.bindPrefWrapLengthToRegion(mainContent, contentFlowPane, 80);
        loadUserInfo();
        loadContent();
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

    private void loadContent() {
        System.out.println("ContentViewController: Loading content page " + currentPage);
        contentFlowPane.getChildren().clear();
        ContentDAO contentDAO = new ContentDAO();
        var contentList = contentDAO.findAllPaginated(currentPage, pageSize);
        
        System.out.println("ContentViewController: Found " + (contentList != null ? contentList.size() : "null") + " content items");
        
        if (contentList != null) {
            for (Content content : contentList) {
                System.out.println("ContentViewController: Creating card for content " + content.getId());
                contentFlowPane.getChildren().add(createContentCard(content));
            }
        }
        
        updatePagination();
    }

    private VBox createContentCard(Content content) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1A1D26; -fx-background-radius: 12; -fx-padding: 20; -fx-spacing: 16;");
        card.setPrefWidth(400);
        
        // Content image (if available)
        if (content.getImage() != null && !content.getImage().isEmpty()) {
            ImageView imageView = new ImageView();
            try {
                Image image = new Image(content.getImage(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Use default placeholder if image fails to load
            }
            imageView.setFitHeight(200);
            imageView.setFitWidth(360);
            imageView.setPreserveRatio(false);
            imageView.setStyle("-fx-background-radius: 8; -fx-background-color: #252936;");
            card.getChildren().add(imageView);
        }
        
        // Content title
        Label titleLabel = new Label(content.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        card.getChildren().add(titleLabel);
        
        // Content type
        Label typeLabel = new Label(content.getType());
        typeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        card.getChildren().add(typeLabel);
        
        // Content author and date
        HBox authorBox = new HBox();
        authorBox.setSpacing(10);
        
        Label authorLabel = new Label("By: " + (content.getAuthor() != null ? content.getAuthor().getUsername() : "Unknown"));
        authorLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        
        Label dateLabel = new Label(formatDate(content.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.40); -fx-font-size: 12px;");
        
        authorBox.getChildren().addAll(authorLabel, dateLabel);
        card.getChildren().add(authorBox);
        
        // Content body
        Label bodyLabel = new Label(content.getBody());
        bodyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.80); -fx-font-size: 14px;");
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxHeight(100);
        card.getChildren().add(bodyLabel);
        
        // Like/Dislike section
        HBox actionBox = new HBox(10);

        LikeDAO countsDao = new LikeDAO();
        long initialLikes = countsDao.countLikesByContent(content.getId());
        long initialDislikes = countsDao.countDislikesByContent(content.getId());
        
        final int contentId = content.getId();

        Button likeButton = new Button("👍 " + initialLikes);
        likeButton.setStyle("-fx-background-color: #2D7A3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");

        Button dislikeButton = new Button("👎 " + initialDislikes);
        dislikeButton.setStyle("-fx-background-color: #A04030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");

        likeButton.setOnAction(e -> {
            System.out.println("Like button clicked for content " + contentId);
            new Thread(() -> {
                try {
                    var currentUser = SessionManager.getCurrentUser();
                    if (currentUser == null) {
                        System.err.println("User not logged in");
                        return;
                    }

                    LikeDAO likeDAO = new LikeDAO();
                    likeDAO.toggleContentReactionByIdsJdbc(currentUser.getId(), contentId, "like");
                    System.out.println("Like saved successfully");

                    Platform.runLater(() -> {
                        try {
                            long likes = likeDAO.countLikesByContent(contentId);
                            long dislikes = likeDAO.countDislikesByContent(contentId);
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
            }, "content-like-" + contentId).start();
        });

        dislikeButton.setOnAction(e -> {
            System.out.println("Dislike button clicked for content " + contentId);
            new Thread(() -> {
                try {
                    var currentUser = SessionManager.getCurrentUser();
                    if (currentUser == null) {
                        System.err.println("User not logged in");
                        return;
                    }

                    LikeDAO likeDAO = new LikeDAO();
                    likeDAO.toggleContentReactionByIdsJdbc(currentUser.getId(), contentId, "dislike");
                    System.out.println("Dislike saved successfully");

                    Platform.runLater(() -> {
                        try {
                            long likes = likeDAO.countLikesByContent(contentId);
                            long dislikes = likeDAO.countDislikesByContent(contentId);
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
            }, "content-dislike-" + contentId).start();
        });
        
        actionBox.getChildren().addAll(likeButton, dislikeButton);
        card.getChildren().add(actionBox);
        
        // Comment section with toggle
        VBox commentSection = new VBox();
        commentSection.setSpacing(10);
        
        // Toggle button for comments
        Button toggleCommentsButton = new Button("Show Comments");
        toggleCommentsButton.setStyle("-fx-background-color: #3D2FA0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 12px;");
        commentSection.getChildren().add(toggleCommentsButton);
        
        // Comments container (initially hidden)
        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(10);
        commentsContainer.setVisible(false);
        commentsContainer.setManaged(false);
        
        Label commentHeader = new Label("Comments");
        commentHeader.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        commentsContainer.getChildren().add(commentHeader);
        
        // Load existing comments
        GuideCommentDAO commentDAO = new GuideCommentDAO();
        java.util.List<GuideComment> comments = commentDAO.findByContent(content.getId());
        
        if (comments != null && !comments.isEmpty()) {
            for (GuideComment comment : comments) {
                VBox commentBox = new VBox();
                commentBox.setSpacing(4);
                commentBox.setStyle("-fx-background-color: #252936; -fx-background-radius: 8; -fx-padding: 10;");
                
                Label commentAuthor = new Label(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                commentAuthor.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold;");
                
                Label commentText = new Label(comment.getContent());
                commentText.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
                commentText.setWrapText(true);
                
                commentBox.getChildren().addAll(commentAuthor, commentText);
                commentsContainer.getChildren().add(commentBox);
            }
        } else {
            Label noCommentsLabel = new Label("No comments yet");
            noCommentsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
            commentsContainer.getChildren().add(noCommentsLabel);
        }
        
        // Add comment input
        HBox commentInputBox = new HBox();
        commentInputBox.setSpacing(10);
        
        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Write a comment...");
        commentInput.setPrefRowCount(2);
        commentInput.setStyle("-fx-background-color: #252936; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #AAAAAA; -fx-control-inner-background: #252936; -fx-background-radius: 6; -fx-border-color: #3D2FA0; -fx-font-size: 13px;");
        HBox.setHgrow(commentInput, javafx.scene.layout.Priority.ALWAYS);
        
        Button submitCommentButton = new Button("Post");
        submitCommentButton.setStyle("-fx-background-color: #3D2FA0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-size: 13px;");
        submitCommentButton.setOnAction(e -> handleAddComment(content, commentInput));
        
        commentInputBox.getChildren().addAll(commentInput, submitCommentButton);
        commentsContainer.getChildren().add(commentInputBox);
        
        commentSection.getChildren().add(commentsContainer);
        
        // Toggle button action
        toggleCommentsButton.setOnAction(e -> {
            boolean isVisible = commentsContainer.isVisible();
            commentsContainer.setVisible(!isVisible);
            commentsContainer.setManaged(!isVisible);
            toggleCommentsButton.setText(isVisible ? "Show Comments" : "Hide Comments");
        });
        
        card.getChildren().add(commentSection);
        
        return card;
    }

    private void updatePagination() {
        ContentDAO contentDAO = new ContentDAO();
        long totalContent = contentDAO.countAll();
        int totalPages = (int) Math.ceil((double) totalContent / pageSize);
        
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        previousPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private void handleAddComment(Content content, TextArea commentInput) {
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Please log in to comment");
            return;
        }
        
        String commentText = commentInput.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Please enter a comment");
            return;
        }
        
        ContentDAO contentDAO = new ContentDAO();
        com.nexusplay.dao.UserDAO userDAO = new com.nexusplay.dao.UserDAO();
        
        // Fetch fresh instances from database
        com.nexusplay.entity.User freshUser = userDAO.findById(currentUser.getId());
        Content freshContent = contentDAO.findById(content.getId());
        
        if (freshUser == null || freshContent == null) {
            showAlert("Error: Could not load user or content");
            return;
        }
        
        GuideCommentDAO commentDAO = new GuideCommentDAO();
        GuideComment newComment = new GuideComment(freshContent, commentText, freshUser);
        commentDAO.save(newComment);
        
        commentInput.clear();
        
        // Refresh content
        contentFlowPane.getChildren().clear();
        loadContent();
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleNextPage() {
        try {
            ContentDAO contentDAO = new ContentDAO();
            int totalPages = (int) Math.ceil((double) contentDAO.countAll() / pageSize);
            if (currentPage < totalPages) {
                currentPage++;
                loadContent();
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
                loadContent();
            }
        } catch (Exception e) {
            System.err.println("Error in handlePreviousPage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearSession();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            System.err.println("Error in handleLogout: " + e.getMessage());
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
            System.err.println("Error in goToHome: " + e.getMessage());
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
            System.err.println("Error in showGames: " + e.getMessage());
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
            System.err.println("Error in showShop: " + e.getMessage());
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
            System.err.println("Error in showForum: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showContent() {
        try {
            // Reload content when already on content page
            System.out.println("ContentViewController: showContent() called");
            currentPage = 1;
            loadContent();
        } catch (Exception e) {
            System.err.println("Error in showContent: " + e.getMessage());
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
            System.err.println("Error in showStreams: " + e.getMessage());
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
            System.err.println("Error in showPlayers: " + e.getMessage());
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
            System.err.println("Error in showCoaches: " + e.getMessage());
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
            System.err.println("Error in showOrganizations: " + e.getMessage());
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
            System.err.println("Error in showTeams: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
