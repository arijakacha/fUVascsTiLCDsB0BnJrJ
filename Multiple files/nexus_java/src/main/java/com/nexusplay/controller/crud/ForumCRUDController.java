package com.nexusplay.controller.crud;

import com.nexusplay.dao.ForumPostDAO;
import com.nexusplay.dao.GuideCommentDAO;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.ForumPost;
import com.nexusplay.entity.GuideComment;
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

public class ForumCRUDController extends CRUDController<ForumPost> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private ForumPostDAO forumPostDAO = new ForumPostDAO();
    private UserDAO userDAO = new UserDAO();
    private GuideCommentDAO guideCommentDAO = new GuideCommentDAO();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<ForumPost> filteredPosts = FXCollections.observableArrayList();
    
    private TextField currentTitleField;
    private Button currentImageUploadButton;
    private Label currentImageLabel;
    private TextArea currentContentField;
    private ComboBox<User> currentUserCombo;
    private String selectedImagePath;

    @FXML
    public void initialize() {
        loadUsers();
        setupButtons();
        loadCards();
    }

    protected void loadUsers() {
        try {
            List<User> userList = userDAO.findAllHibernate();
            users.clear();
            
            if (userList != null && !userList.isEmpty()) {
                users.addAll(userList);
                System.out.println("ForumCRUD: Loaded " + userList.size() + " users via Hibernate");
                for (User u : userList) {
                    System.out.println("  - User: " + u.getUsername() + " (id=" + u.getId() + ")");
                }
            } else {
                System.out.println("ForumCRUD: No users loaded via Hibernate, trying JDBC fallback");
                List<User> jdbcUsers = userDAO.findAll();
                users.clear();
                if (jdbcUsers != null && !jdbcUsers.isEmpty()) {
                    users.addAll(jdbcUsers);
                    System.out.println("ForumCRUD: Loaded " + jdbcUsers.size() + " users via JDBC");
                    for (User u : jdbcUsers) {
                        System.out.println("  - User: " + u.getUsername() + " (id=" + u.getId() + ")");
                    }
                } else {
                    System.out.println("ForumCRUD: No users found via either method");
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            
            // Try JDBC fallback on error
            try {
                List<User> jdbcUsers = userDAO.findAll();
                users.clear();
                if (jdbcUsers != null && !jdbcUsers.isEmpty()) {
                    users.addAll(jdbcUsers);
                    System.out.println("ForumCRUD: Loaded " + jdbcUsers.size() + " users via JDBC (fallback)");
                }
            } catch (Exception ex) {
                System.out.println("JDBC fallback also failed: " + ex.getMessage());
            }
        }
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            ForumPost newPost = createNewItem();
            showForumPostDialog(newPost, "Add New Forum Post");
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
        if (filteredPosts.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Posts");
            alert.setHeaderText(null);
            alert.setContentText("No forum posts found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<ForumPost> postsToEdit = filteredPosts.isEmpty() ? allItems : filteredPosts;
        
        ChoiceDialog<ForumPost> dialog = new ChoiceDialog<>(postsToEdit.get(0), postsToEdit);
        dialog.setTitle("Edit Forum Post");
        dialog.setHeaderText("Select a forum post to edit");
        dialog.setContentText("Choose a forum post from the list:");
        
        dialog.showAndWait().ifPresent(post -> {
            editItem(post);
        });
    }
    
    private void showDeleteDialog() {
        if (filteredPosts.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Posts");
            alert.setHeaderText(null);
            alert.setContentText("No forum posts found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<ForumPost> postsToDelete = filteredPosts.isEmpty() ? allItems : filteredPosts;
        
        ChoiceDialog<ForumPost> dialog = new ChoiceDialog<>(postsToDelete.get(0), postsToDelete);
        dialog.setTitle("Delete Forum Post");
        dialog.setHeaderText("Select a forum post to delete");
        dialog.setContentText("Choose a forum post from the list:");
        
        dialog.showAndWait().ifPresent(post -> {
            deleteItem(post);
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
    protected List<ForumPost> loadAllItems() {
        return forumPostDAO.findAll();
    }

    @Override
    protected void deleteItem(ForumPost item) {
        forumPostDAO.delete(item);
        loadCards();
    }

    @Override
    protected ForumPost createNewItem() {
        ForumPost post = new ForumPost();
        post.setCreatedAt(java.time.LocalDateTime.now());
        return post;
    }

    @Override
    protected void updateItem(ForumPost item) {
    }

    @Override
    protected boolean saveItem(ForumPost item) {
        try {
            // Ensure createdAt is set
            if (item.getCreatedAt() == null) {
                item.setCreatedAt(java.time.LocalDateTime.now());
            }
            
            // Re-attach the User to the session to avoid detached entity error
            if (item.getUser() != null && item.getUser().getId() != null) {
                User attachedUser = userDAO.findById(item.getUser().getId());
                if (attachedUser != null) {
                    item.setUser(attachedUser);
                }
            }
            
            System.out.println("Saving ForumPost: title=" + item.getTitle() + ", user_id=" + (item.getUser() != null ? item.getUser().getId() : "null"));
            
            if (item.getId() == null) {
                forumPostDAO.save(item);
            } else {
                forumPostDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            StringBuilder chain = new StringBuilder();
            Throwable t = e;
            Throwable best = e;
            while (t != null) {
                String line = (t.getClass() != null ? t.getClass().getName() : "")
                        + (t.getMessage() != null ? (": " + t.getMessage()) : "");
                if (!line.trim().isEmpty()) {
                    if (chain.length() > 0) chain.append("\nCaused by: ");
                    chain.append(line);
                }

                if (t instanceof java.sql.SQLException) {
                    best = t;
                }
                if (t.getClass() != null) {
                    String cn = t.getClass().getName();
                    if (cn.startsWith("org.hibernate.exception")) {
                        best = t;
                    }
                }

                t = t.getCause();
            }

            String bestLine = (best.getClass() != null ? best.getClass().getName() : "")
                    + (best.getMessage() != null ? (": " + best.getMessage()) : "");
            if (bestLine.trim().isEmpty()) {
                bestLine = String.valueOf(best);
            }

            String errorMsg = "Error saving forum post:\n" + bestLine + "\n\nDetails:\n" + chain;
            System.out.println(errorMsg);
            e.printStackTrace();
            showNotImplemented(errorMsg);
            return false;
        }
    }

    protected void loadCards() {
        List<ForumPost> posts = loadAllItems();
        allItems = posts;
        countLabel.setText(posts.size() + " posts");
        
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) posts.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    protected void filterCards(String searchText) {
        List<ForumPost> allPosts = loadAllItems();
        allItems = allPosts;
        String searchLower = searchText != null ? searchText.toLowerCase() : "";
        
        filteredPosts.clear();
        for (ForumPost post : allPosts) {
            boolean matches = searchLower.isEmpty() || 
                (post.getTitle() != null && post.getTitle().toLowerCase().contains(searchLower)) ||
                (post.getContent() != null && post.getContent().toLowerCase().contains(searchLower));
            
            if (matches) {
                filteredPosts.add(post);
            }
        }
        
        countLabel.setText(filteredPosts.size() + " posts");
        
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) filteredPosts.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }
    
    private VBox createPage(int pageIndex) {
        int itemsPerPage = 6;
        List<ForumPost> postsToShow = (filteredPosts.isEmpty()) ? allItems : filteredPosts;
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, postsToShow.size());
        
        VBox page = new VBox(15);
        page.setStyle("-fx-padding: 10;");
        
        FlowPane pageFlowPane = new FlowPane(15, 15);
        pageFlowPane.setStyle("-fx-padding: 10;");
        
        for (int i = fromIndex; i < toIndex; i++) {
            pageFlowPane.getChildren().add(createForumPostCard(postsToShow.get(i)));
        }
        
        if (postsToShow.isEmpty()) {
            Label emptyLabel = new Label("No forum posts found in database.\nClick '+ Add Post' button to add posts manually.");
            emptyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 16px; -fx-font-style: italic; -fx-text-alignment: center;");
            pageFlowPane.getChildren().add(emptyLabel);
        }
        
        page.getChildren().add(pageFlowPane);
        return page;
    }

    private User getCurrentUser() {
        try {
            return userDAO.findByUsername("hedi");
        } catch (Exception e) {
            System.out.println("Error getting current user: " + e.getMessage());
            List<User> users = userDAO.findAll();
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }
            return null;
        }
    }

    private void showCommentsDialog(ForumPost post) {
        System.out.println("Opening comments dialog for post: " + post.getId());
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Comments");
            dialog.setHeaderText("Comments for: " + (post.getTitle() != null ? post.getTitle() : "Post"));
            dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));
            content.setStyle("-fx-background-color: #1a1a2e;");

            // Load existing comments
            List<GuideComment> comments = guideCommentDAO.findByForumPost(post.getId());
            System.out.println("Loaded " + (comments != null ? comments.size() : 0) + " comments");
            
            // Comments list
            VBox commentsList = new VBox(10);
            commentsList.setStyle("-fx-background-color: #0B0D14; -fx-background-radius: 8; -fx-padding: 15; -fx-pref-width: 400; -fx-pref-height: 300;");
            
            if (comments != null && !comments.isEmpty()) {
                for (GuideComment comment : comments) {
                    VBox commentBox = new VBox(5);
                    commentBox.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 6; -fx-padding: 10; -fx-border-color: #2D3142; -fx-border-radius: 6; -fx-border-width: 1;");
                    
                    Label authorLabel = new Label(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown");
                    authorLabel.setStyle("-fx-text-fill: #667eea; -fx-font-size: 12px; -fx-font-weight: bold;");
                    
                    Label contentLabel = new Label(comment.getContent());
                    contentLabel.setStyle("-fx-text-fill: #E0E7FF; -fx-font-size: 13px; -fx-wrap-text: true;");
                    contentLabel.setPrefWidth(370);
                    
                    Label dateLabel = new Label(comment.getCreatedAt() != null ? comment.getCreatedAt().toString().substring(0, 16) : "");
                    dateLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 10px;");
                    
                    // Delete button for admin
                    Button deleteCommentBtn = new Button("🗑️ Delete");
                    deleteCommentBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 4;");
                    deleteCommentBtn.setOnAction(e -> {
                        try {
                            guideCommentDAO.delete(comment);
                            showCommentsDialog(post); // Refresh dialog
                        } catch (Exception ex) {
                            showNotImplemented("Error deleting comment: " + ex.getMessage());
                        }
                    });
                    
                    HBox commentHeader = new HBox(10);
                    commentHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    commentHeader.getChildren().addAll(authorLabel, deleteCommentBtn);
                    
                    commentBox.getChildren().addAll(commentHeader, contentLabel, dateLabel);
                    commentsList.getChildren().add(commentBox);
                }
            } else {
                Label noCommentsLabel = new Label("No comments yet. Be the first to comment!");
                noCommentsLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 14px; -fx-font-style: italic;");
                commentsList.getChildren().add(noCommentsLabel);
            }

            ScrollPane scrollPane = new ScrollPane(commentsList);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);

            // Add comment section
            TextArea newCommentField = new TextArea();
            newCommentField.setPromptText("Write a comment...");
            newCommentField.setPrefRowCount(3);
            newCommentField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: #E0E7FF; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

            Button addCommentBtn = new Button("💬 Add Comment");
            addCommentBtn.setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; -fx-font-size: 14px;");

            addCommentBtn.setOnAction(e -> {
                String commentText = newCommentField.getText().trim();
                if (!commentText.isEmpty()) {
                    try {
                        GuideComment newComment = new GuideComment();
                        newComment.setPost(post);
                        newComment.setContent(commentText);
                        newComment.setUser(getCurrentUser());
                        newComment.setCreatedAt(java.time.LocalDateTime.now());
                        
                        guideCommentDAO.save(newComment);
                        showCommentsDialog(post); // Refresh dialog
                    } catch (Exception ex) {
                        showNotImplemented("Error adding comment: " + ex.getMessage());
                    }
                }
            });

            content.getChildren().addAll(scrollPane, newCommentField, addCommentBtn);

            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);
            dialog.getDialogPane().setContent(content);

            dialog.showAndWait();
        } catch (Exception e) {
            System.out.println("Error opening comments dialog: " + e.getMessage());
            e.printStackTrace();
            showNotImplemented("Error opening comments dialog: " + e.getMessage());
        }
    }

    protected VBox createForumPostCard(ForumPost post) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Header with icon
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -fx-background-radius: 8;");
        iconPane.setPrefSize(250, 80);
        iconPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label postIcon = new Label("💬");
        postIcon.setStyle("-fx-font-size: 36px;");
        iconPane.getChildren().add(postIcon);
        
        // Post title
        String postTitle = post.getTitle() != null ? post.getTitle() : "No Title";
        if (postTitle.length() > 35) {
            postTitle = postTitle.substring(0, 35) + "...";
        }
        Label titleLabel = new Label(postTitle);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5 0;");
        
        // Author
        String authorName = "Unknown Author";
        try {
            if (post.getUser() != null && post.getUser().getUsername() != null) {
                authorName = post.getUser().getUsername();
            }
        } catch (Exception e) {
            authorName = "Unknown Author";
        }
        Label authorLabel = new Label("👤 " + authorName);
        authorLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Content preview
        Label contentLabel = new Label(post.getContent() != null && post.getContent().length() > 60 ? 
            post.getContent().substring(0, 60) + "..." : post.getContent());
        contentLabel.setStyle("-fx-text-fill: #E0E7FF; -fx-font-size: 11px; -fx-wrap-text: true;");
        contentLabel.setPrefWidth(250);
        
        // Created date
        String dateStr = post.getCreatedAt() != null ? post.getCreatedAt().toString().substring(0, 10) : "N/A";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button commentsBtn = new Button("💬 Comments");
        commentsBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        commentsBtn.setOnAction(e -> showCommentsDialog(post));
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(post));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(post));
        
        actionsBox.getChildren().addAll(commentsBtn, editBtn, deleteBtn);
        
        card.getChildren().addAll(iconPane, titleLabel, authorLabel, contentLabel, dateLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #667eea; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(ForumPost item) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label(item.getId() == null ? "Add New Forum Post" : "Edit Forum Post");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        currentTitleField = new TextField(item.getTitle());
        currentTitleField.setPromptText("Post Title");
        currentTitleField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        // Image upload section
        selectedImagePath = item.getImage();
        currentImageUploadButton = new Button("📁 Upload Image");
        currentImageUploadButton.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 16; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-font-size: 14px;");
        currentImageUploadButton.setOnAction(e -> handleImageUpload());
        
        currentImageLabel = new Label(selectedImagePath != null ? selectedImagePath : "No image selected");
        currentImageLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px; -fx-wrap-text: true;");
        currentImageLabel.setPrefWidth(300);

        currentContentField = new TextArea(item.getContent());
        currentContentField.setPromptText("Post Content");
        currentContentField.setPrefRowCount(5);
        currentContentField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: #E0E7FF; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentUserCombo = new ComboBox<>(users);
        currentUserCombo.setValue(item.getUser());
        currentUserCombo.setPromptText("Select Author");
        currentUserCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentUserCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "Select Author";
                try {
                    String username = user.getUsername();
                    return username != null ? username : "User";
                } catch (Exception e) {
                    return "User";
                }
            }
            @Override
            public User fromString(String string) {
                return null;
            }
        });

        Label titleLabelLabel = new Label("Title:");
        titleLabelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label imageLabel = new Label("Image:");
        imageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label contentLabel = new Label("Content:");
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label userLabel = new Label("Author:");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            titleLabel,
            new javafx.scene.layout.Region(),
            titleLabelLabel, currentTitleField,
            imageLabel, currentImageUploadButton, currentImageLabel,
            contentLabel, currentContentField,
            userLabel, currentUserCombo
        );

        return content;
    }

    @Override
    protected boolean validateInput(ForumPost item) {
        String title = currentTitleField == null ? "" : currentTitleField.getText().trim();
        String content = currentContentField == null ? "" : currentContentField.getText().trim();

        if (title.isEmpty()) {
            showNotImplemented("Title is required");
            return false;
        }
        if (title.length() < 4) {
            showNotImplemented("Title must be at least 4 characters");
            return false;
        }
        if (title.length() > 10000) {
            showNotImplemented("Title must be less than 10000 characters");
            return false;
        }

        if (content.isEmpty()) {
            showNotImplemented("Content is required");
            return false;
        }
        if (content.length() < 4) {
            showNotImplemented("Content must be at least 4 characters");
            return false;
        }
        if (content.length() > 10000) {
            showNotImplemented("Content must be less than 10000 characters");
            return false;
        }

        if (currentUserCombo.getValue() == null) {
            showNotImplemented("Author selection is required");
            return false;
        }

        item.setTitle(title);
        item.setImage(selectedImagePath);
        item.setContent(content);
        item.setUser(currentUserCombo.getValue());

        return true;
    }

    @Override
    protected void addItem() {
        ForumPost newPost = createNewItem();
        showForumPostDialog(newPost, "Add New Forum Post");
    }

    @Override
    protected void editItem(ForumPost item) {
        showForumPostDialog(item, "Edit Forum Post");
    }

    private void showForumPostDialog(ForumPost post, String title) {
        loadUsers();
        System.out.println("ForumCRUD: Opening dialog, users list size = " + users.size());
        
        Dialog<ForumPost> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(post);
        dialog.getDialogPane().setContent(content);
        
        System.out.println("ForumCRUD: ComboBox has " + currentUserCombo.getItems().size() + " items");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(post)) {
                    return post;
                }
            }
            return null;
        });

        Optional<ForumPost> result = dialog.showAndWait();
        result.ifPresent(savedPost -> {
            if (saveItem(savedPost)) {
            }
        });
    }

    private void handleImageUpload() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        java.io.File selectedFile = fileChooser.showOpenDialog(currentImageUploadButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            currentImageLabel.setText(selectedImagePath);
        }
    }

    private void showNotImplemented(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setExpandableContent(new javafx.scene.control.TextArea(message));
        alert.getDialogPane().setExpanded(false);
        alert.showAndWait();
    }
}
