package com.nexusplay.controller.crud;

import com.nexusplay.entity.Content;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.dao.ContentDAO;
import com.nexusplay.dao.GuideCommentDAO;
import com.nexusplay.entity.User;
import com.nexusplay.entity.GuideComment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class ContentCRUDController extends CRUDController<Content> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private ContentDAO contentDAO = new ContentDAO();
    private UserDAO userDAO = new UserDAO();
    private GuideCommentDAO guideCommentDAO = new GuideCommentDAO();
    private ObservableList<Content> allItems = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();
    
    private TextField currentTitleField;
    private TextField currentTypeField;
    private TextField currentImageField;
    private TextArea currentBodyField;
    private ComboBox<User> currentAuthorCombo;

    @FXML
    public void initialize() {
        loadUsers();
        setupButtons();
        loadCards();
    }

    protected void loadUsers() {
        users.addAll(userDAO.findAll());
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Content newContent = createNewItem();
            showContentDialog(newContent, "Add New Content");
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

    private void showEditDialog() {
        List<Content> contents = loadAllItems();
        if (contents.isEmpty()) {
            showNotImplemented("No content available to edit");
            return;
        }

        Dialog<Content> dialog = new Dialog<>();
        dialog.setTitle("Select Content to Edit");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Content> contentCombo = new ComboBox<>(FXCollections.observableArrayList(contents));
        contentCombo.setPromptText("Select Content");
        contentCombo.setConverter(new javafx.util.StringConverter<Content>() {
            @Override
            public String toString(Content content) {
                if (content == null) return "Select Content";
                try {
                    String title = content.getTitle();
                    return title != null ? title : "Content";
                } catch (Exception e) {
                    return "Content";
                }
            }
            @Override
            public Content fromString(String string) {
                return null;
            }
        });

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");
        content.getChildren().addAll(new Label("Content:"), contentCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(selectButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return contentCombo.getValue();
            }
            return null;
        });

        Optional<Content> result = dialog.showAndWait();
        result.ifPresent(selectedContent -> {
            editItem(selectedContent);
        });
    }

    private void showDeleteDialog() {
        List<Content> contents = loadAllItems();
        if (contents.isEmpty()) {
            showNotImplemented("No content available to delete");
            return;
        }

        Dialog<Content> dialog = new Dialog<>();
        dialog.setTitle("Select Content to Delete");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Content> contentCombo = new ComboBox<>(FXCollections.observableArrayList(contents));
        contentCombo.setPromptText("Select Content");
        contentCombo.setConverter(new javafx.util.StringConverter<Content>() {
            @Override
            public String toString(Content content) {
                if (content == null) return "Select Content";
                try {
                    String title = content.getTitle();
                    return title != null ? title : "Content";
                } catch (Exception e) {
                    return "Content";
                }
            }
            @Override
            public Content fromString(String string) {
                return null;
            }
        });

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(25));
        dialogContent.setStyle("-fx-background-color: #1a1a2e;");
        dialogContent.getChildren().addAll(new Label("Content:"), contentCombo);
        dialog.getDialogPane().setContent(dialogContent);

        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(deleteButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return contentCombo.getValue();
            }
            return null;
        });

        Optional<Content> result = dialog.showAndWait();
        result.ifPresent(selectedContent -> {
            deleteItem(selectedContent);
        });
    }

    @Override
    protected void setupTableView() {
        // Not using table view - using cards instead
    }

    @Override
    protected List<Content> loadAllItems() {
        return contentDAO.findAll();
    }

    @Override
    protected void deleteItem(Content item) {
        contentDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Content newContent = createNewItem();
        showContentDialog(newContent, "Add New Content");
    }

    @Override
    protected void editItem(Content item) {
        showContentDialog(item, "Edit Content");
    }

    @Override
    protected Content createNewItem() {
        Content content = new Content();
        content.setCreatedAt(java.time.LocalDateTime.now());
        return content;
    }

    @Override
    protected void updateItem(Content item) {
    }

    @Override
    protected boolean saveItem(Content item) {
        try {
            if (item.getId() == null) {
                contentDAO.save(item);
            } else {
                contentDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving content: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        try {
            List<Content> contents = loadAllItems();
            allItems.clear();
            allItems.addAll(contents);
            
            countLabel.setText(contents.size() + " content");
            
            pagination.setPageFactory(this::createPage);
            pagination.setPageCount((int) Math.ceil((double) contents.size() / 6.0));
            pagination.setCurrentPageIndex(0);
        } catch (Exception e) {
            System.out.println("Error loading cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void filterCards(String searchText) {
        List<Content> allContents = loadAllItems();
        allItems.clear();
        
        for (Content content : allContents) {
            if (content.getTitle() != null && content.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                allItems.add(content);
            }
        }
        
        countLabel.setText(allItems.size() + " content");
        
        int pageCount = (int) Math.ceil((double) allItems.size() / 6.0);
        pagination.setPageFactory(this::createPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
    }

    protected VBox createPage(int pageIndex) {
        FlowPane flowPane = new FlowPane(15, 15);
        flowPane.setStyle("-fx-background-color: transparent;");
        
        int fromIndex = pageIndex * 6;
        int toIndex = Math.min(fromIndex + 6, allItems.size());
        
        if (fromIndex < allItems.size()) {
            List<Content> pageItems = allItems.subList(fromIndex, toIndex);
            for (Content content : pageItems) {
                flowPane.getChildren().add(createContentCard(content));
            }
        }
        
        VBox page = new VBox(10);
        page.getChildren().add(flowPane);
        return page;
    }

    protected VBox createContentCard(Content content) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Content image
        javafx.scene.layout.StackPane imagePane = new javafx.scene.layout.StackPane();
        imagePane.setStyle("-fx-background-color: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); -fx-background-radius: 8;");
        imagePane.setPrefSize(250, 120);
        imagePane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label contentIcon = new Label("📝");
        contentIcon.setStyle("-fx-font-size: 48px;");
        imagePane.getChildren().add(contentIcon);
        
        // Content title
        Label titleLabel = new Label(content.getTitle() != null && content.getTitle().length() > 30 ? 
            content.getTitle().substring(0, 30) + "..." : content.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Type
        Label typeLabel = new Label(content.getType() != null ? "🏷️ " + content.getType() : "🏷️ Type: N/A");
        typeLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Author
        Label authorLabel = new Label(content.getAuthor() != null ? "✍️ " + content.getAuthor().getUsername() : "✍️ Author: N/A");
        authorLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Body preview
        Label bodyLabel = new Label(content.getBody() != null && content.getBody().length() > 50 ? 
            content.getBody().substring(0, 50) + "..." : content.getBody());
        bodyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px; -fx-wrap-text: true;");
        bodyLabel.setPrefWidth(250);
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button commentsBtn = new Button("💬 Comments");
        commentsBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        commentsBtn.setOnAction(e -> showCommentsDialog(content));
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(content));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(content));
        
        actionsBox.getChildren().addAll(commentsBtn, editBtn, deleteBtn);
        
        card.getChildren().addAll(imagePane, titleLabel, typeLabel, authorLabel, bodyLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #a8edea; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
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

    private void showCommentsDialog(Content content) {
        System.out.println("Opening comments dialog for content: " + content.getId());
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Comments");
            dialog.setHeaderText("Comments for: " + (content.getTitle() != null ? content.getTitle() : "Content"));
            dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

            VBox dialogContent = new VBox(15);
            dialogContent.setPadding(new javafx.geometry.Insets(20));
            dialogContent.setStyle("-fx-background-color: #1a1a2e;");

            // Load existing comments
            List<GuideComment> comments = guideCommentDAO.findByContent(content.getId());
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
                            showCommentsDialog(content); // Refresh dialog
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
                        newComment.setGuide(content);
                        newComment.setContent(commentText);
                        newComment.setUser(getCurrentUser());
                        newComment.setCreatedAt(java.time.LocalDateTime.now());
                        
                        guideCommentDAO.save(newComment);
                        showCommentsDialog(content); // Refresh dialog
                    } catch (Exception ex) {
                        showNotImplemented("Error adding comment: " + ex.getMessage());
                    }
                }
            });

            dialogContent.getChildren().addAll(scrollPane, newCommentField, addCommentBtn);

            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);
            dialog.getDialogPane().setContent(dialogContent);

            dialog.showAndWait();
        } catch (Exception e) {
            System.out.println("Error opening comments dialog: " + e.getMessage());
            e.printStackTrace();
            showNotImplemented("Error opening comments dialog: " + e.getMessage());
        }
    }

    @Override
    protected VBox createDialogContent(Content item) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        currentTitleField = new TextField(item.getTitle());
        currentTitleField.setPromptText("Content Title");

        currentTypeField = new TextField(item.getType());
        currentTypeField.setPromptText("Content Type");

        currentImageField = new TextField(item.getImage());
        currentImageField.setPromptText("Image URL");

        currentBodyField = new TextArea(item.getBody());
        currentBodyField.setPromptText("Content Body");
        currentBodyField.setPrefRowCount(5);

        currentAuthorCombo = new ComboBox<>(users);
        currentAuthorCombo.setValue(item.getAuthor());
        currentAuthorCombo.setPromptText("Select Author");

        content.getChildren().addAll(
            new Label("Title:"), currentTitleField,
            new Label("Type:"), currentTypeField,
            new Label("Image URL:"), currentImageField,
            new Label("Body:"), currentBodyField,
            new Label("Author:"), currentAuthorCombo
        );

        return content;
    }

    @Override
    protected boolean validateInput(Content item) {
        String title = currentTitleField == null ? "" : currentTitleField.getText().trim();
        String type = currentTypeField == null ? "" : currentTypeField.getText().trim();
        String body = currentBodyField == null ? "" : currentBodyField.getText().trim();
        String image = currentImageField == null ? "" : currentImageField.getText().trim();

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

        if (type.isEmpty()) {
            showNotImplemented("Type is required");
            return false;
        }
        if (type.length() < 4) {
            showNotImplemented("Type must be at least 4 characters");
            return false;
        }
        if (type.length() > 10000) {
            showNotImplemented("Type must be less than 10000 characters");
            return false;
        }

        if (body.isEmpty()) {
            showNotImplemented("Body is required");
            return false;
        }
        if (body.length() < 4) {
            showNotImplemented("Body must be at least 4 characters");
            return false;
        }
        if (body.length() > 10000) {
            showNotImplemented("Body must be less than 10000 characters");
            return false;
        }

        if (image.length() > 10000) {
            showNotImplemented("Image URL must be less than 10000 characters");
            return false;
        }

        if (currentAuthorCombo.getValue() == null) {
            showNotImplemented("Author selection is required");
            return false;
        }

        item.setTitle(title);
        item.setType(type);
        item.setImage(image);
        item.setBody(body);
        item.setAuthor(currentAuthorCombo.getValue());

        return true;
    }

    private void showContentDialog(Content content, String title) {
        loadUsers();
        
        Dialog<Content> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox contentBox = createDialogContent(content);
        dialog.getDialogPane().setContent(contentBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(content)) {
                    return content;
                }
            }
            return null;
        });

        Optional<Content> result = dialog.showAndWait();
        result.ifPresent(savedContent -> {
            if (saveItem(savedContent)) {
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
