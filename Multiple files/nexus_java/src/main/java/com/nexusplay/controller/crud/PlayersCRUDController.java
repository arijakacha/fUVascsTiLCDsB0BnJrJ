package com.nexusplay.controller.crud;

import com.nexusplay.dao.GameDAO;
import com.nexusplay.dao.PlayerDAO;
import com.nexusplay.dao.TeamDAO;
import com.nexusplay.entity.Game;
import com.nexusplay.entity.Player;
import com.nexusplay.entity.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class PlayersCRUDController extends CRUDController<Player> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private PlayerDAO playerDAO = new PlayerDAO();
    private GameDAO gameDAO = new GameDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private ObservableList<Game> games = FXCollections.observableArrayList();
    private ObservableList<Team> teams = FXCollections.observableArrayList();
    private ObservableList<Player> filteredPlayers = FXCollections.observableArrayList();
    
    private TextField currentNicknameField;
    private ComboBox<Game> currentGameCombo;
    private ComboBox<Team> currentTeamCombo;

    @FXML
    public void initialize() {
        setupButtons();
        loadCards();
    }

    protected void loadGames() {
        try {
            List<Game> gameList = gameDAO.findAll();
            games.clear();
            if (gameList != null) {
                games.addAll(gameList);
            }
        } catch (Exception e) {
            System.out.println("Error loading games: " + e.getMessage());
        }
    }

    protected void loadTeams() {
        try {
            List<Team> teamList = teamDAO.findAll();
            teams.clear();
            if (teamList != null) {
                teams.addAll(teamList);
            }
        } catch (Exception e) {
            System.out.println("Error loading teams: " + e.getMessage());
        }
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Player newPlayer = createNewItem();
            showPlayerDialog(newPlayer, "Add New Player");
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
        // Show a dialog to select a player to edit
        if (filteredPlayers.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Players");
            alert.setHeaderText(null);
            alert.setContentText("No players found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Player> playersToEdit = filteredPlayers.isEmpty() ? allItems : filteredPlayers;
        
        // Create a selection dialog
        ChoiceDialog<Player> dialog = new ChoiceDialog<>(playersToEdit.get(0), playersToEdit);
        dialog.setTitle("Edit Player");
        dialog.setHeaderText("Select a player to edit");
        dialog.setContentText("Choose a player from the list:");
        
        dialog.showAndWait().ifPresent(player -> {
            editItem(player);
        });
    }
    
    private void showDeleteDialog() {
        // Show a dialog to select a player to delete
        if (filteredPlayers.isEmpty() && (allItems == null || allItems.isEmpty())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Players");
            alert.setHeaderText(null);
            alert.setContentText("No players found in the database.");
            alert.showAndWait();
            return;
        }
        
        List<Player> playersToDelete = filteredPlayers.isEmpty() ? allItems : filteredPlayers;
        
        // Create a selection dialog
        ChoiceDialog<Player> dialog = new ChoiceDialog<>(playersToDelete.get(0), playersToDelete);
        dialog.setTitle("Delete Player");
        dialog.setHeaderText("Select a player to delete");
        dialog.setContentText("Choose a player from the list:");
        
        dialog.showAndWait().ifPresent(player -> {
            deleteItem(player);
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
    protected List<Player> loadAllItems() {
        return playerDAO.findAll();
    }

    @Override
    protected void deleteItem(Player item) {
        playerDAO.delete(item);
        loadCards();
    }

    @Override
    protected Player createNewItem() {
        Player player = new Player();
        return player;
    }

    @Override
    protected void updateItem(Player item) {
    }

    @Override
    protected boolean saveItem(Player item) {
        try {
            if (item.getId() == null) {
                playerDAO.save(item);
            } else {
                playerDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving player: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        List<Player> players = loadAllItems();
        allItems = players;
        countLabel.setText(players.size() + " players");
        
        // Setup pagination
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) players.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    protected void filterCards(String searchText) {
        List<Player> allPlayers = loadAllItems();
        allItems = allPlayers;
        String searchLower = searchText != null ? searchText.toLowerCase() : "";
        
        filteredPlayers.clear();
        for (Player player : allPlayers) {
            boolean matches = searchLower.isEmpty() || 
                (player.getNickname() != null && player.getNickname().toLowerCase().contains(searchLower));
            
            if (matches) {
                filteredPlayers.add(player);
            }
        }
        
        countLabel.setText(filteredPlayers.size() + " players");
        
        // Setup pagination
        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) filteredPlayers.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }
    
    private VBox createPage(int pageIndex) {
        int itemsPerPage = 6;
        List<Player> playersToShow = (filteredPlayers.isEmpty()) ? allItems : filteredPlayers;
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, playersToShow.size());
        
        VBox page = new VBox(15);
        page.setStyle("-fx-padding: 10;");
        
        FlowPane pageFlowPane = new FlowPane(15, 15);
        pageFlowPane.setStyle("-fx-padding: 10;");
        
        for (int i = fromIndex; i < toIndex; i++) {
            pageFlowPane.getChildren().add(createPlayerCard(playersToShow.get(i)));
        }
        
        if (playersToShow.isEmpty()) {
            Label emptyLabel = new Label("No players found in database.\nClick '+ Add Player' button to add players manually.");
            emptyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 16px; -fx-font-style: italic; -fx-text-alignment: center;");
            pageFlowPane.getChildren().add(emptyLabel);
        }
        
        page.getChildren().add(pageFlowPane);
        return page;
    }

    protected VBox createPlayerCard(Player player) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 250;");
        
        // Header
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        javafx.scene.layout.StackPane avatarPane = new javafx.scene.layout.StackPane();
        avatarPane.setStyle("-fx-background-color: #3D2FA0; -fx-background-radius: 24;");
        avatarPane.setPrefSize(48, 48);
        avatarPane.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label avatarLabel = new Label(player.getNickname() != null ? player.getNickname().substring(0, 1).toUpperCase() : "P");
        avatarLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        avatarPane.getChildren().add(avatarLabel);
        
        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(player.getNickname() != null ? player.getNickname() : "Unknown");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        String gameName = "No Game";
        try {
            if (player.getGame() != null) {
                gameName = player.getGame().getName();
            }
        } catch (Exception e) {
            gameName = "No Game";
        }
        Label gameLabel = new Label(gameName);
        gameLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        infoBox.getChildren().addAll(nameLabel, gameLabel);
        headerBox.getChildren().addAll(avatarPane, infoBox);
        
        // Team info
        String teamName = "No Team";
        try {
            if (player.getTeam() != null) {
                teamName = player.getTeam().getName();
            }
        } catch (Exception e) {
            teamName = "No Team";
        }
        Label teamLabel = new Label("🏢 " + teamName);
        teamLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(player));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(player));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(headerBox, teamLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #3D2FA0; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 250;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 250;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Player item) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label(item.getId() == null ? "Add New Player" : "Edit Player");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        currentNicknameField = new TextField(item.getNickname());
        currentNicknameField.setPromptText("Player Nickname");
        currentNicknameField.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");

        currentGameCombo = new ComboBox<>(games);
        currentGameCombo.setValue(item.getGame());
        currentGameCombo.setPromptText("Select Game");
        currentGameCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentGameCombo.setConverter(new javafx.util.StringConverter<Game>() {
            @Override
            public String toString(Game game) {
                if (game == null) return "Select Game";
                try {
                    String name = game.getName();
                    return name != null ? name : "Game";
                } catch (Exception e) {
                    return "Game";
                }
            }
            @Override
            public Game fromString(String string) {
                return null;
            }
        });

        currentTeamCombo = new ComboBox<>(teams);
        currentTeamCombo.setValue(item.getTeam());
        currentTeamCombo.setPromptText("Select Team");
        currentTeamCombo.setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-prompt-text-fill: #8B92B9; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 12 16; -fx-font-size: 14px;");
        currentTeamCombo.setConverter(new javafx.util.StringConverter<Team>() {
            @Override
            public String toString(Team team) {
                if (team == null) return "Select Team";
                try {
                    String name = team.getName();
                    return name != null ? name : "Team";
                } catch (Exception e) {
                    return "Team";
                }
            }
            @Override
            public Team fromString(String string) {
                return null;
            }
        });

        Label nicknameLabel = new Label("Nickname:");
        nicknameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label gameLabel = new Label("Game:");
        gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label teamLabel = new Label("Team:");
        teamLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(
            titleLabel,
            new javafx.scene.layout.Region(),
            nicknameLabel, currentNicknameField,
            gameLabel, currentGameCombo,
            teamLabel, currentTeamCombo
        );

        return content;
    }

    @Override
    protected boolean validateInput(Player item) {
        String nickname = currentNicknameField == null ? "" : currentNicknameField.getText().trim();

        if (nickname.isEmpty()) {
            showNotImplemented("Nickname is required");
            return false;
        }
        if (nickname.length() < 4) {
            showNotImplemented("Nickname must be at least 4 characters");
            return false;
        }
        if (nickname.length() > 10000) {
            showNotImplemented("Nickname must be less than 10000 characters");
            return false;
        }

        if (currentGameCombo.getValue() == null) {
            showNotImplemented("Game selection is required");
            return false;
        }

        item.setNickname(nickname);
        item.setGame(currentGameCombo.getValue());
        item.setTeam(currentTeamCombo.getValue());

        return true;
    }

    @Override
    protected void addItem() {
        Player newPlayer = createNewItem();
        showPlayerDialog(newPlayer, "Add New Player");
    }

    @Override
    protected void editItem(Player item) {
        showPlayerDialog(item, "Edit Player");
    }

    private void showPlayerDialog(Player player, String title) {
        // Load games and teams before showing dialog to avoid lazy loading errors
        loadGames();
        loadTeams();
        
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style the buttons
        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(player);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(player)) {
                    return player;
                }
            }
            return null;
        });

        Optional<Player> result = dialog.showAndWait();
        result.ifPresent(savedPlayer -> {
            if (saveItem(savedPlayer)) {
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
