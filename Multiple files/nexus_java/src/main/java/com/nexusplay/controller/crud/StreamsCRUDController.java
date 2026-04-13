package com.nexusplay.controller.crud;

import com.nexusplay.entity.Stream;
import com.nexusplay.dao.PlayerDAO;
import com.nexusplay.dao.StreamDAO;
import com.nexusplay.entity.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.DatePicker;

import java.util.List;
import java.util.Optional;

public class StreamsCRUDController extends CRUDController<Stream> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private StreamDAO streamDAO = new StreamDAO();
    private PlayerDAO playerDAO = new PlayerDAO();
    private ObservableList<Stream> allItems = FXCollections.observableArrayList();
    private ObservableList<Player> players = FXCollections.observableArrayList();
    
    private TextField currentTitleField;
    private TextField currentUrlField;
    private ComboBox<Player> currentPlayerCombo;
    private CheckBox currentIsLiveCheck;
    private DatePicker currentReunionDatePicker;

    @FXML
    public void initialize() {
        System.out.println("StreamsCRUD: Initializing...");
        System.out.println("StreamsCRUD: pagination = " + (pagination != null ? "injected" : "NULL"));
        System.out.println("StreamsCRUD: contentArea = " + (contentArea != null ? "injected" : "NULL"));
        System.out.println("StreamsCRUD: btnAdd = " + (btnAdd != null ? "injected" : "NULL"));
        loadPlayers();
        setupButtons();
        loadCards();
        System.out.println("StreamsCRUD: Initialization complete");
    }

    protected void loadPlayers() {
        try {
            List<Player> playerList = playerDAO.findAll();
            players.clear();
            if (playerList != null) {
                players.addAll(playerList);
                System.out.println("StreamsCRUD: Loaded " + playerList.size() + " players");
                for (Player p : playerList) {
                    System.out.println("  - Player: " + p.getNickname() + " (id=" + p.getId() + ")");
                }
            } else {
                System.out.println("StreamsCRUD: No players found");
            }
        } catch (Exception e) {
            System.out.println("Error loading players: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Stream newStream = createNewItem();
            showStreamDialog(newStream, "Add New Stream");
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
        List<Stream> streams = loadAllItems();
        if (streams.isEmpty()) {
            showNotImplemented("No streams available to edit");
            return;
        }

        Dialog<Stream> dialog = new Dialog<>();
        dialog.setTitle("Select Stream to Edit");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Stream> streamCombo = new ComboBox<>(FXCollections.observableArrayList(streams));
        streamCombo.setPromptText("Select Stream");
        streamCombo.setConverter(new javafx.util.StringConverter<Stream>() {
            @Override
            public String toString(Stream stream) {
                if (stream == null) return "Select Stream";
                try {
                    String title = stream.getTitle();
                    return title != null ? title : "Stream";
                } catch (Exception e) {
                    return "Stream";
                }
            }
            @Override
            public Stream fromString(String string) {
                return null;
            }
        });

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");
        content.getChildren().addAll(new Label("Stream:"), streamCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(selectButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return streamCombo.getValue();
            }
            return null;
        });

        Optional<Stream> result = dialog.showAndWait();
        result.ifPresent(stream -> {
            editItem(stream);
        });
    }

    private void showDeleteDialog() {
        List<Stream> streams = loadAllItems();
        if (streams.isEmpty()) {
            showNotImplemented("No streams available to delete");
            return;
        }

        Dialog<Stream> dialog = new Dialog<>();
        dialog.setTitle("Select Stream to Delete");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Stream> streamCombo = new ComboBox<>(FXCollections.observableArrayList(streams));
        streamCombo.setPromptText("Select Stream");
        streamCombo.setConverter(new javafx.util.StringConverter<Stream>() {
            @Override
            public String toString(Stream stream) {
                if (stream == null) return "Select Stream";
                try {
                    String title = stream.getTitle();
                    return title != null ? title : "Stream";
                } catch (Exception e) {
                    return "Stream";
                }
            }
            @Override
            public Stream fromString(String string) {
                return null;
            }
        });

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");
        content.getChildren().addAll(new Label("Stream:"), streamCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(deleteButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return streamCombo.getValue();
            }
            return null;
        });

        Optional<Stream> result = dialog.showAndWait();
        result.ifPresent(stream -> {
            deleteItem(stream);
        });
    }

    @Override
    protected void setupTableView() {
        // Not using table view - using cards instead
    }

    @Override
    protected List<Stream> loadAllItems() {
        return streamDAO.findAll();
    }

    @Override
    protected void deleteItem(Stream item) {
        streamDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Stream newStream = createNewItem();
        showStreamDialog(newStream, "Add New Stream");
    }

    @Override
    protected void editItem(Stream item) {
        showStreamDialog(item, "Edit Stream");
    }

    @Override
    protected Stream createNewItem() {
        Stream stream = new Stream();
        stream.setCreatedAt(java.time.LocalDateTime.now());
        stream.setIsLive(false);
        return stream;
    }

    @Override
    protected void updateItem(Stream item) {
    }

    @Override
    protected boolean saveItem(Stream item) {
        try {
            if (item.getId() == null) {
                streamDAO.save(item);
            } else {
                streamDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving stream: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        try {
            List<Stream> streams = loadAllItems();
            allItems.clear();
            allItems.addAll(streams);
            
            System.out.println("StreamsCRUD: Loaded " + streams.size() + " streams");
            for (Stream s : streams) {
                System.out.println("  - Stream: " + s.getTitle() + " (id=" + s.getId() + ")");
            }
            
            countLabel.setText(streams.size() + " streams");
            
            int pageCount = (int) Math.ceil((double) streams.size() / 6.0);
            System.out.println("StreamsCRUD: Setting page count to " + pageCount);
            
            pagination.setPageFactory(this::createPage);
            pagination.setPageCount(pageCount);
            pagination.setCurrentPageIndex(0);
            
            System.out.println("StreamsCRUD: Pagination configured, current page index = " + pagination.getCurrentPageIndex());
        } catch (Exception e) {
            System.out.println("Error loading cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void filterCards(String searchText) {
        System.out.println("StreamsCRUD: filterCards called with search text: '" + searchText + "'");
        List<Stream> allStreams = loadAllItems();
        allItems.clear();
        
        for (Stream stream : allStreams) {
            if (stream.getTitle() != null && stream.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                allItems.add(stream);
                System.out.println("StreamsCRUD: Matched stream: " + stream.getTitle());
            }
        }
        
        System.out.println("StreamsCRUD: Filtered to " + allItems.size() + " streams");
        countLabel.setText(allItems.size() + " streams");
        
        int pageCount = (int) Math.ceil((double) allItems.size() / 6.0);
        pagination.setPageFactory(this::createPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        
        System.out.println("StreamsCRUD: Search complete, page count = " + pagination.getPageCount());
    }

    protected VBox createPage(int pageIndex) {
        System.out.println("StreamsCRUD: createPage called for page " + pageIndex + ", allItems.size() = " + allItems.size());
        FlowPane flowPane = new FlowPane(15, 15);
        flowPane.setStyle("-fx-background-color: transparent;");
        
        int fromIndex = pageIndex * 6;
        int toIndex = Math.min(fromIndex + 6, allItems.size());
        
        System.out.println("StreamsCRUD: fromIndex = " + fromIndex + ", toIndex = " + toIndex);
        
        if (fromIndex < allItems.size()) {
            List<Stream> pageItems = allItems.subList(fromIndex, toIndex);
            System.out.println("StreamsCRUD: Creating " + pageItems.size() + " cards for page " + pageIndex);
            for (Stream stream : pageItems) {
                flowPane.getChildren().add(createStreamCard(stream));
            }
        } else {
            System.out.println("StreamsCRUD: No items to display for page " + pageIndex);
        }
        
        VBox page = new VBox(10);
        page.getChildren().add(flowPane);
        return page;
    }

    protected VBox createStreamCard(Stream stream) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Stream icon
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -fx-background-radius: 8;");
        iconPane.setPrefSize(250, 100);
        iconPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label streamIcon = new Label("📺");
        streamIcon.setStyle("-fx-font-size: 48px;");
        iconPane.getChildren().add(streamIcon);
        
        // Stream title
        Label titleLabel = new Label(stream.getTitle() != null && stream.getTitle().length() > 30 ? 
            stream.getTitle().substring(0, 30) + "..." : stream.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Player
        Label playerLabel = new Label(stream.getPlayer() != null ? "🎮 " + stream.getPlayer().getNickname() : "🎮 Player: N/A");
        playerLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Live status
        Label liveLabel = new Label(stream.getIsLive() != null && stream.getIsLive() ? "🔴 LIVE" : "⚫ Offline");
        liveLabel.setStyle(stream.getIsLive() != null && stream.getIsLive() ? 
            "-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-font-weight: bold;" : 
            "-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Reunion date
        String reunionText = "";
        if (stream.getReunionDate() != null) {
            reunionText = "📅 " + stream.getReunionDate().toLocalDate().toString();
        }
        Label reunionLabel = new Label(reunionText);
        reunionLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px;");
        
        // URL
        Label urlLabel = new Label(stream.getUrl() != null && stream.getUrl().length() > 30 ? 
            stream.getUrl().substring(0, 30) + "..." : stream.getUrl());
        urlLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 11px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(stream));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(stream));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(iconPane, titleLabel, playerLabel, liveLabel, reunionLabel, urlLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #667eea; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Stream item) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label(item.getId() == null ? "Add New Stream" : "Edit Stream");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        currentPlayerCombo = new ComboBox<>(players);
        currentPlayerCombo.setValue(item.getPlayer());
        currentPlayerCombo.setPromptText("Select Player");
        currentPlayerCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentPlayerCombo.setConverter(new javafx.util.StringConverter<Player>() {
            @Override
            public String toString(Player player) {
                if (player == null) return "Select Player";
                try {
                    String nickname = player.getNickname();
                    return nickname != null ? nickname : "Player";
                } catch (Exception e) {
                    return "Player";
                }
            }
            @Override
            public Player fromString(String string) {
                return null;
            }
        });

        currentTitleField = new TextField(item.getTitle());
        currentTitleField.setPromptText("Stream Title");
        currentTitleField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentUrlField = new TextField(item.getUrl());
        currentUrlField.setPromptText("Stream URL");
        currentUrlField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentIsLiveCheck = new CheckBox("Is Live");
        currentIsLiveCheck.setSelected(item.getIsLive() != null ? item.getIsLive() : false);
        currentIsLiveCheck.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        currentReunionDatePicker = new DatePicker();
        if (item.getReunionDate() != null) {
            currentReunionDatePicker.setValue(item.getReunionDate().toLocalDate());
        }
        currentReunionDatePicker.setPromptText("Reunion Date (optional)");
        currentReunionDatePicker.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        Label playerLabel = new Label("Player:");
        playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label titleLabelLabel = new Label("Title:");
        titleLabelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label urlLabel = new Label("URL:");
        urlLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label reunionDateLabel = new Label("Reunion Date:");
        reunionDateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            titleLabel,
            new javafx.scene.layout.Region(),
            playerLabel, currentPlayerCombo,
            titleLabelLabel, currentTitleField,
            urlLabel, currentUrlField,
            currentIsLiveCheck,
            reunionDateLabel, currentReunionDatePicker
        );

        return content;
    }

    @Override
    protected boolean validateInput(Stream item) {
        if (currentPlayerCombo.getValue() == null) {
            showNotImplemented("Player selection is required");
            return false;
        }

        String title = currentTitleField == null ? "" : currentTitleField.getText().trim();
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

        String url = currentUrlField == null ? "" : currentUrlField.getText().trim();
        if (url.isEmpty()) {
            showNotImplemented("URL is required");
            return false;
        }
        if (url.length() < 4) {
            showNotImplemented("URL must be at least 4 characters");
            return false;
        }
        if (url.length() > 10000) {
            showNotImplemented("URL must be less than 10000 characters");
            return false;
        }

        item.setPlayer(currentPlayerCombo.getValue());
        item.setTitle(title);
        item.setUrl(url);
        item.setIsLive(currentIsLiveCheck.isSelected());
        
        if (currentReunionDatePicker.getValue() != null) {
            item.setReunionDate(currentReunionDatePicker.getValue().atStartOfDay());
        } else {
            item.setReunionDate(null);
        }

        return true;
    }

    private void showStreamDialog(Stream stream, String title) {
        System.out.println("StreamsCRUD: Opening dialog - " + title);
        loadPlayers();
        System.out.println("StreamsCRUD: Dialog - players list size = " + players.size());
        
        Dialog<Stream> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(stream);
        dialog.getDialogPane().setContent(content);
        
        System.out.println("StreamsCRUD: Dialog content created, showing dialog");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(stream)) {
                    return stream;
                }
            }
            return null;
        });

        Optional<Stream> result = dialog.showAndWait();
        result.ifPresent(savedStream -> {
            if (saveItem(savedStream)) {
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
