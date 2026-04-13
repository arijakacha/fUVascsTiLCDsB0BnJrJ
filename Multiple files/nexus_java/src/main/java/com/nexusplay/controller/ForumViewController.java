package com.nexusplay.controller;

import com.nexusplay.dao.ForumPostDAO;
import com.nexusplay.dao.GuideCommentDAO;
import com.nexusplay.dao.LikeDAO;
import com.nexusplay.entity.ForumPost;
import com.nexusplay.entity.GuideComment;
import com.nexusplay.entity.Like;
import com.nexusplay.util.FlowLayoutHelper;
import com.nexusplay.util.SessionManager;
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

public class ForumViewController extends BaseController {

    @FXML private Label userNameLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label avatarLabel;
    @FXML private VBox mainContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane forumPostsFlowPane;
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
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageLabel;

    private int currentPage = 0;
    private static final int POSTS_PER_PAGE = 6;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        bindWindowChrome();
        FlowLayoutHelper.bindPrefWrapLengthToRegion(mainContent, forumPostsFlowPane, 80);
        loadUserInfo();
        loadPosts();
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

    private void loadPosts() {
        ForumPostDAO forumPostDAO = new ForumPostDAO();
        java.util.List<ForumPost> posts = forumPostDAO.findAllPaginated(currentPage, POSTS_PER_PAGE);
        long totalPosts = forumPostDAO.countAll();
        totalPages = (int) Math.ceil((double) totalPosts / POSTS_PER_PAGE);
        
        forumPostsFlowPane.getChildren().clear();
        
        if (posts != null && !posts.isEmpty()) {
            for (ForumPost post : posts) {
                forumPostsFlowPane.getChildren().add(createPostCard(post));
            }
        } else {
            Label noPostsLabel = new Label("No forum posts available");
            noPostsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 16px;");
            forumPostsFlowPane.getChildren().add(noPostsLabel);
        }
        
        updatePaginationControls();
    }
    
    private void updatePaginationControls() {
        if (pageLabel != null) {
            pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        }
        if (prevPageButton != null) {
            prevPageButton.setDisable(currentPage == 0);
        }
        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages - 1 || totalPages == 0);
        }
    }
    
    @FXML
    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadPosts();
        }
    }
    
    @FXML
    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadPosts();
        }
    }

    private VBox createPostCard(ForumPost post) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1A1D26; -fx-background-radius: 12; -fx-padding: 20; -fx-spacing: 16;");
        card.setPrefWidth(400);
        
        // Post image (if available)
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            ImageView imageView = new ImageView();
            try {
                Image image = new Image(post.getImage(), true);
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
        
        // Post title
        Label titleLabel = new Label(post.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        card.getChildren().add(titleLabel);
        
        // Post author and date
        HBox authorBox = new HBox();
        authorBox.setSpacing(10);
        
        Label authorLabel = new Label("Posted by: " + (post.getUser() != null ? post.getUser().getUsername() : "Unknown"));
        authorLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 12px;");
        
        Label dateLabel = new Label(formatDate(post.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.40); -fx-font-size: 12px;");
        
        authorBox.getChildren().addAll(authorLabel, dateLabel);
        card.getChildren().add(authorBox);
        
        // Post content
        Label contentLabel = new Label(post.getContent());
        contentLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.80); -fx-font-size: 14px;");
        contentLabel.setWrapText(true);
        contentLabel.setMaxHeight(100);
        card.getChildren().add(contentLabel);
        
        // Like/Dislike buttons
        HBox actionBox = new HBox();
        actionBox.setSpacing(10);
        
        LikeDAO likeDAO = new LikeDAO();
        long likes = likeDAO.countLikesByPost(post.getId());
        long dislikes = likeDAO.countDislikesByPost(post.getId());
        
        Button likeButton = new Button("👍 " + likes);
        likeButton.setStyle("-fx-background-color: #2D7A3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");
        likeButton.setOnAction(e -> handleLike(post));
        
        Button dislikeButton = new Button("👎 " + dislikes);
        dislikeButton.setStyle("-fx-background-color: #A04030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-size: 13px;");
        dislikeButton.setOnAction(e -> handleDislike(post));
        
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
        java.util.List<GuideComment> comments = commentDAO.findByForumPost(post.getId());
        
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
        submitCommentButton.setOnAction(e -> handleAddComment(post, commentInput));
        
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

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private void handleLike(ForumPost post) {
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Please log in to like posts");
            return;
        }
        
        try {
            LikeDAO likeDAO = new LikeDAO();
            Like existingLike = likeDAO.findByUserAndPost(currentUser.getId(), post.getId());
            
            if (existingLike != null) {
                if (existingLike.getType().equals("like")) {
                    // Unlike
                    likeDAO.delete(existingLike);
                } else {
                    // Change from dislike to like - delete and recreate
                    likeDAO.delete(existingLike);
                    likeDAO.savePostLikeByIds(currentUser.getId(), post.getId(), "like");
                }
            } else {
                // New like
                likeDAO.savePostLikeByIds(currentUser.getId(), post.getId(), "like");
            }
            
            // Refresh posts
            forumPostsFlowPane.getChildren().clear();
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    private void handleDislike(ForumPost post) {
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Please log in to dislike posts");
            return;
        }
        
        try {
            LikeDAO likeDAO = new LikeDAO();
            Like existingLike = likeDAO.findByUserAndPost(currentUser.getId(), post.getId());
            
            if (existingLike != null) {
                if (existingLike.getType().equals("dislike")) {
                    // Remove dislike
                    likeDAO.delete(existingLike);
                } else {
                    // Change from like to dislike - delete and recreate
                    likeDAO.delete(existingLike);
                    likeDAO.savePostLikeByIds(currentUser.getId(), post.getId(), "dislike");
                }
            } else {
                // New dislike
                likeDAO.savePostLikeByIds(currentUser.getId(), post.getId(), "dislike");
            }
            
            // Refresh posts
            forumPostsFlowPane.getChildren().clear();
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    private void handleAddComment(ForumPost post, TextArea commentInput) {
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
        
        ForumPostDAO forumPostDAO = new ForumPostDAO();
        com.nexusplay.dao.UserDAO userDAO = new com.nexusplay.dao.UserDAO();
        
        // Fetch fresh instances from database
        com.nexusplay.entity.User freshUser = userDAO.findById(currentUser.getId());
        ForumPost freshPost = forumPostDAO.findById(post.getId());
        
        if (freshUser == null || freshPost == null) {
            showAlert("Error: Could not load user or post");
            return;
        }
        
        GuideCommentDAO commentDAO = new GuideCommentDAO();
        GuideComment newComment = new GuideComment(freshPost, commentText, freshUser);
        commentDAO.save(newComment);
        
        commentInput.clear();
        
        // Refresh posts
        forumPostsFlowPane.getChildren().clear();
        loadPosts();
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        // Already on forum page
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
