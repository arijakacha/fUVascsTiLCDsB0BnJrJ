package com.nexusplay.controller.crud;

import com.nexusplay.dao.GameDAO;
import com.nexusplay.entity.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class GamesCRUDController extends CRUDController<Game> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private GameDAO gameDAO = new GameDAO();
    private ObservableList<Game> allItems = FXCollections.observableArrayList();
    
    private TextField currentNameField;
    private TextField currentLogoField;
    private TextArea currentDescriptionField;
    private TextField currentReleaseYearField;

    @FXML
    public void initialize() {
        setupButtons();
        loadCards();
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Game newGame = createNewItem();
            showGameDialog(newGame, "Add New Game");
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
        List<Game> games = loadAllItems();
        if (games.isEmpty()) {
            showNotImplemented("No games available to edit");
            return;
        }

        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Select Game to Edit");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Game> gameCombo = new ComboBox<>(FXCollections.observableArrayList(games));
        gameCombo.setPromptText("Select Game");
        gameCombo.setConverter(new javafx.util.StringConverter<Game>() {
            @Override
            public String toString(Game game) {
                if (game == null) return "Select Game";
                return game.getName() != null ? game.getName() : "Game";
            }
            @Override
            public Game fromString(String string) {
                return null;
            }
        });

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(25));
        dialogContent.setStyle("-fx-background-color: #1a1a2e;");
        dialogContent.getChildren().addAll(new Label("Game:"), gameCombo);
        dialog.getDialogPane().setContent(dialogContent);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(selectButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return gameCombo.getValue();
            }
            return null;
        });

        Optional<Game> result = dialog.showAndWait();
        result.ifPresent(selectedGame -> {
            editItem(selectedGame);
        });
    }

    private void showDeleteDialog() {
        List<Game> games = loadAllItems();
        if (games.isEmpty()) {
            showNotImplemented("No games available to delete");
            return;
        }

        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Select Game to Delete");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Game> gameCombo = new ComboBox<>(FXCollections.observableArrayList(games));
        gameCombo.setPromptText("Select Game");
        gameCombo.setConverter(new javafx.util.StringConverter<Game>() {
            @Override
            public String toString(Game game) {
                if (game == null) return "Select Game";
                return game.getName() != null ? game.getName() : "Game";
            }
            @Override
            public Game fromString(String string) {
                return null;
            }
        });

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(25));
        dialogContent.setStyle("-fx-background-color: #1a1a2e;");
        dialogContent.getChildren().addAll(new Label("Game:"), gameCombo);
        dialog.getDialogPane().setContent(dialogContent);

        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(deleteButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return gameCombo.getValue();
            }
            return null;
        });

        Optional<Game> result = dialog.showAndWait();
        result.ifPresent(selectedGame -> {
            deleteItem(selectedGame);
        });
    }

    @Override
    protected void setupTableView() {
        // Not using table view - using cards instead
    }

    @Override
    protected List<Game> loadAllItems() {
        return gameDAO.findAll();
    }

    @Override
    protected void deleteItem(Game item) {
        gameDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Game newGame = createNewItem();
        showGameDialog(newGame, "Add New Game");
    }

    @Override
    protected void editItem(Game item) {
        showGameDialog(item, "Edit Game");
    }

    @Override
    protected Game createNewItem() {
        Game game = new Game();
        return game;
    }

    @Override
    protected void updateItem(Game item) {
    }

    @Override
    protected boolean saveItem(Game item) {
        try {
            if (item.getId() == null) {
                gameDAO.save(item);
            } else {
                gameDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving game: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        try {
            List<Game> games = loadAllItems();
            allItems.clear();
            allItems.addAll(games);
            
            countLabel.setText((games != null ? games.size() : 0) + " games");
            
            pagination.setPageFactory(this::createPage);
            pagination.setPageCount((int) Math.ceil((double) (games != null ? games.size() : 0) / 6.0));
            pagination.setCurrentPageIndex(0);
        } catch (Exception e) {
            System.out.println("Error loading cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void filterCards(String searchText) {
        List<Game> allGames = loadAllItems();
        allItems.clear();
        
        for (Game game : allGames) {
            if (game.getName() != null && game.getName().toLowerCase().contains(searchText.toLowerCase())) {
                allItems.add(game);
            }
        }
        
        countLabel.setText(allItems.size() + " games");
        
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
            List<Game> pageItems = allItems.subList(fromIndex, toIndex);
            for (Game game : pageItems) {
                flowPane.getChildren().add(createGameCard(game));
            }
        }
        
        VBox page = new VBox(10);
        page.getChildren().add(flowPane);
        return page;
    }

    protected VBox createGameCard(Game game) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Game logo
        javafx.scene.layout.StackPane logoPane = new javafx.scene.layout.StackPane();
        logoPane.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -fx-background-radius: 8;");
        logoPane.setPrefSize(250, 140);
        logoPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        if (game.getLogo() != null && !game.getLogo().isEmpty()) {
            try {
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(game.getLogo(), true)); // background loading
                imageView.setFitWidth(250);
                imageView.setFitHeight(140);
                imageView.setPreserveRatio(false);
                logoPane.getChildren().add(imageView);
            } catch (Exception e) {
                System.out.println("Invalid game logo path: " + game.getLogo());
                Label gameIcon = new Label(game.getName() != null && game.getName().length() > 0 ? game.getName().substring(0, 1).toUpperCase() : "G");
                gameIcon.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
                logoPane.getChildren().add(gameIcon);
            }
        } else {
            Label gameIcon = new Label(game.getName() != null && game.getName().length() > 0 ? game.getName().substring(0, 1).toUpperCase() : "G");
            gameIcon.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
            logoPane.getChildren().add(gameIcon);
        }
        
        // Game name
        Label nameLabel = new Label(game.getName() != null ? game.getName() : "Unknown Game");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Release year
        Label yearLabel = new Label(game.getReleaseYear() != null ? "Released: " + game.getReleaseYear() : "Release Year: N/A");
        yearLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Description
        Label descLabel = new Label(game.getDescription() != null && game.getDescription().length() > 60 ? 
            game.getDescription().substring(0, 60) + "..." : game.getDescription());
        descLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px; -fx-wrap-text: true;");
        descLabel.setPrefWidth(250);
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(game));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(game));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(logoPane, nameLabel, yearLabel, descLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #667eea; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Game item) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        currentNameField = new TextField(item.getName());
        currentNameField.setPromptText("Game Name");

        currentLogoField = new TextField(item.getLogo());
        currentLogoField.setPromptText("Logo URL");

        currentDescriptionField = new TextArea(item.getDescription());
        currentDescriptionField.setPromptText("Description");
        currentDescriptionField.setPrefRowCount(3);

        currentReleaseYearField = new TextField(item.getReleaseYear() != null ? item.getReleaseYear().toString() : "");
        currentReleaseYearField.setPromptText("Release Year");

        content.getChildren().addAll(
            new Label("Name:"), currentNameField,
            new Label("Logo URL:"), currentLogoField,
            new Label("Description:"), currentDescriptionField,
            new Label("Release Year:"), currentReleaseYearField
        );

        return content;
    }

    @Override
    protected boolean validateInput(Game item) {
        String nameText = currentNameField != null ? currentNameField.getText().trim() : "";
        String logoText = currentLogoField != null ? currentLogoField.getText().trim() : "";
        String descriptionText = currentDescriptionField != null ? currentDescriptionField.getText().trim() : "";
        String yearText = currentReleaseYearField != null ? currentReleaseYearField.getText().trim() : "";

        if (nameText.isEmpty()) {
            showNotImplemented("Game name is required");
            return false;
        }
        if (nameText.length() < 4) {
            showNotImplemented("Game name must be at least 4 characters");
            return false;
        }
        if (nameText.length() > 10000) {
            showNotImplemented("Game name must be less than 10000 characters");
            return false;
        }

        if (logoText.length() > 10000) {
            showNotImplemented("Logo URL must be less than 10000 characters");
            return false;
        }

        if (descriptionText.length() > 10000) {
            showNotImplemented("Description must be less than 10000 characters");
            return false;
        }

        if (yearText.isEmpty()) {
            showNotImplemented("Release year is required");
            return false;
        }
        if (yearText.length() < 4) {
            showNotImplemented("Release year must be at least 4 characters");
            return false;
        }
        if (yearText.length() > 10000) {
            showNotImplemented("Release year must be less than 10000 characters");
            return false;
        }

        item.setName(nameText);
        item.setLogo(logoText);
        item.setDescription(descriptionText);
        
        try {
            item.setReleaseYear(Short.parseShort(yearText));
        } catch (NumberFormatException e) {
            showNotImplemented("Release year must be a valid number");
            return false;
        }

        return true;
    }

    private void showGameDialog(Game game, String title) {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(game);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(game)) {
                    return game;
                }
            }
            return null;
        });

        Optional<Game> result = dialog.showAndWait();
        result.ifPresent(savedGame -> {
            if (saveItem(savedGame)) {
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
