package com.nexusplay.controller;

import com.nexusplay.MainApp;
import com.nexusplay.config.DatabaseConnection;
import com.nexusplay.dao.GameDAO;
import com.nexusplay.dao.NotificationDAO;
import com.nexusplay.dao.PlayerDAO;
import com.nexusplay.dao.TeamDAO;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.Game;
import com.nexusplay.entity.Notification;
import com.nexusplay.entity.Player;
import com.nexusplay.entity.Team;
import com.nexusplay.entity.User;
import com.nexusplay.util.SceneNavigation;
import com.nexusplay.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;




import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboardController extends BaseController implements DashboardController {

    private User currentUser;

    @FXML private Label adminNameLabel;
    @FXML private Label avatarLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalPlayersLabel;
    @FXML private Label revenueLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label shopBadge;
    @FXML private Label ordersBadge;
    @FXML private PieChart entityChart;
    @FXML private Label chartTotalLabel;
    @FXML private VBox mainContent;
    @FXML private Label notificationButton;
    @FXML private Label notificationBadge;

    // Tab-based admin UI (admin-dashboard.fxml)
    @FXML private TabPane mainTabPane;
    @FXML private Tab dashboardTab;
    @FXML private Tab usersTab;
    @FXML private Tab playersTab;
    @FXML private Tab teamsTab;
    @FXML private Tab gamesTab;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Number> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> userTypeColumn;
    @FXML private TableColumn<User, String> statusColumn;

    @FXML private TableView<Player> playersTable;
    @FXML private TableColumn<Player, Number> playerIdColumn;
    @FXML private TableColumn<Player, String> nicknameColumn;
    @FXML private TableColumn<Player, String> playerGameColumn;
    @FXML private TableColumn<Player, String> playerTeamColumn;

    @FXML private TableView<Team> teamsTable;
    @FXML private TableColumn<Team, Number> teamIdColumn;
    @FXML private TableColumn<Team, String> teamNameColumn;
    @FXML private TableColumn<Team, String> teamGameColumn;
    @FXML private TableColumn<Team, String> teamCountryColumn;

    @FXML private TableView<Game> gamesTable;
    @FXML private TableColumn<Game, Number> gameIdColumn;
    @FXML private TableColumn<Game, String> gameNameColumn;
    @FXML private TableColumn<Game, Number> releaseYearColumn;

    private final UserDAO userDAO = new UserDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @FXML
    public void initialize() {
        bindWindowChrome();
        Platform.runLater(() -> {
            Scene scene = mainContent != null ? mainContent.getScene() : (mainTabPane != null ? mainTabPane.getScene() : null);
            if (scene != null) {
                MainApp.bindRootToScene(scene);
            }
        });

        if (SessionManager.getCurrentUser() != null) {
            String name = SessionManager.getCurrentUser().getUsername();
            if (adminNameLabel != null) adminNameLabel.setText(name);
            if (avatarLabel != null && name != null && !name.isEmpty()) {
                avatarLabel.setText(String.valueOf(name.toUpperCase().charAt(0)));
            }
        }

        loadDashboardData();
        updateNotificationBadge();

        configureTablesIfPresent();
    }

    private void configureTablesIfPresent() {
        if (usersTable != null) {
            if (userIdColumn != null) userIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
            if (usernameColumn != null) usernameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
            if (emailColumn != null) emailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
            if (userTypeColumn != null) userTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getUserType())));
            if (statusColumn != null) statusColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
            refreshUsersTable();
        }

        if (gamesTable != null) {
            if (gameIdColumn != null) gameIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
            if (gameNameColumn != null) gameNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
            if (releaseYearColumn != null) {
                releaseYearColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getReleaseYear() == null ? 0 : c.getValue().getReleaseYear()));
            }
            refreshGamesTable();
        }

        if (teamsTable != null) {
            if (teamIdColumn != null) teamIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
            if (teamNameColumn != null) teamNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
            if (teamCountryColumn != null) teamCountryColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCountry()));
            if (teamGameColumn != null) {
                teamGameColumn.setCellValueFactory(c -> new SimpleStringProperty(safeTeamGameName(c.getValue())));
            }
            refreshTeamsTable();
        }

        if (playersTable != null) {
            if (playerIdColumn != null) playerIdColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
            if (nicknameColumn != null) nicknameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNickname()));
            if (playerGameColumn != null) playerGameColumn.setCellValueFactory(c -> new SimpleStringProperty(safePlayerGameName(c.getValue())));
            if (playerTeamColumn != null) playerTeamColumn.setCellValueFactory(c -> new SimpleStringProperty(safePlayerTeamName(c.getValue())));
            refreshPlayersTable();
        }
    }

    private String safeTeamGameName(Team t) {
        try {
            if (t == null || t.getGame() == null) return "";
            return t.getGame().getName();
        } catch (Exception e) {
            return "";
        }
    }

    private String safePlayerGameName(Player p) {
        try {
            if (p == null || p.getGame() == null) return "";
            return p.getGame().getName();
        } catch (Exception e) {
            return "";
        }
    }

    private String safePlayerTeamName(Player p) {
        try {
            if (p == null || p.getTeam() == null) return "";
            return p.getTeam().getName();
        } catch (Exception e) {
            return "";
        }
    }

    private void ensureCrudViewLoadedAndSelectTab(Tab tabToSelect) {
        if (mainTabPane == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) (mainContent != null ? mainContent.getScene().getWindow() : null);
                if (stage == null) {
                    return;
                }
                SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (tabToSelect != null) {
            mainTabPane.getSelectionModel().select(tabToSelect);
        }
    }

    private void loadDashboardData() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            loadMockData();
            return;
        }

        try (conn) {
            int users = getCount(conn, "SELECT COUNT(*) FROM `user`");
            int players = getCount(conn, "SELECT COUNT(*) FROM player");
            int coaches = getCount(conn, "SELECT COUNT(*) FROM coach");
            int products = getCount(conn, "SELECT COUNT(*) FROM product");
            int pending = getCount(conn, "SELECT COUNT(*) FROM payment WHERE status='PENDING'");
            int revenue = getSum(conn, "SELECT SUM(amount) FROM payment WHERE status='COMPLETED'");

            totalUsersLabel.setText(String.valueOf(users));
            totalPlayersLabel.setText(String.valueOf(players));
            revenueLabel.setText("$" + revenue);
            pendingOrdersLabel.setText(String.valueOf(pending));

            shopBadge.setText(products + " New");
            ordersBadge.setText(pending + " Pending");

            int total = users + players + products + coaches;
            chartTotalLabel.setText(String.valueOf(total));

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                    new PieChart.Data("Users", users),
                    new PieChart.Data("Players", players),
                    new PieChart.Data("Shop", products),
                    new PieChart.Data("Content", coaches)
            );
            entityChart.setData(data);

            Platform.runLater(() -> {
                try {
                    entityChart.lookupAll(".chart-pie").forEach(node ->
                            node.setStyle(node.getStyle() + " -fx-border-color: #0F1117; -fx-border-width: 3;"));
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            loadMockData();
        }
    }

    private void loadMockData() {
        totalUsersLabel.setText("29");
        totalPlayersLabel.setText("15");
        revenueLabel.setText("$81");
        pendingOrdersLabel.setText("3");
        chartTotalLabel.setText("53");
        shopBadge.setText("12 New");
        ordersBadge.setText("5 Pending");

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Users", 18),
                new PieChart.Data("Players", 20),
                new PieChart.Data("Shop", 10),
                new PieChart.Data("Content", 5)
        );
        entityChart.setData(data);
    }

    private int getCount(Connection conn, String sql) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getSum(Connection conn, String sql) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @FXML private void showDashboard(ActionEvent e) {
        if (mainTabPane != null && dashboardTab != null) {
            mainTabPane.getSelectionModel().select(dashboardTab);
        } else {
            loadDashboardData();
        }
    }

    @FXML private void showUsers(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UsersCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load users management: " + ex.getMessage());
        }
    }

    @FXML private void showPlayers(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlayersCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load players management: " + ex.getMessage());
        }
    }

    @FXML private void showCoaches(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CoachesCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load coaches management: " + ex.getMessage());
        }
    }

    @FXML private void showOrganizations(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrganizationsCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load organizations management: " + ex.getMessage());
        }
    }

    @FXML private void showGames(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GamesCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load games management: " + ex.getMessage());
        }
    }

    @FXML private void showTeams(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeamsCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load teams management: " + ex.getMessage());
        }
    }

    @FXML private void showShop(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load shop management: " + ex.getMessage());
        }
    }

    @FXML private void showOrders(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrdersCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load orders management: " + ex.getMessage());
        }
    }

    @FXML private void showContent(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ContentCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load content management: " + ex.getMessage());
        }
    }

    @FXML private void showForum(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForumCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load forum management: " + ex.getMessage());
        }
    }

    @FXML private void showStreams(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StreamsCRUD.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
            showNotImplemented("Failed to load streams management: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent e) {
        loadDashboardData();
        updateNotificationBadge();
        refreshUsersTable();
        refreshGamesTable();
        refreshTeamsTable();
        refreshPlayersTable();
    }

    private void updateNotificationBadge() {
        long unreadCount = notificationDAO.countUnread();
        if (notificationBadge != null) {
            if (unreadCount > 0) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        }
    }

    @FXML
    private void handleNotifications(javafx.scene.input.MouseEvent e) {
        System.out.println("DEBUG: Notification button clicked");
        try {
            System.out.println("DEBUG: Loading NotificationsView.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotificationsView.fxml"));
            System.out.println("DEBUG: FXML resource path: " + getClass().getResource("/fxml/NotificationsView.fxml"));
            Parent root = loader.load();
            System.out.println("DEBUG: FXML loaded successfully");
            Stage stage = (Stage) mainContent.getScene().getWindow();
            System.out.println("DEBUG: Stage obtained: " + stage);
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
            System.out.println("DEBUG: Scene navigation completed");
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to load notifications view: " + ex.getMessage());
            ex.printStackTrace();
            showNotImplemented("Failed to load notifications view: " + ex.getMessage());
        }
    }

    @FXML
    private void handleAddUser(ActionEvent e) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add User");
        d.setHeaderText(null);
        d.setContentText("Username:");
        Optional<String> un = d.showAndWait();
        if (un.isEmpty() || un.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog d2 = new TextInputDialog();
        d2.setTitle("Add User");
        d2.setHeaderText(null);
        d2.setContentText("Email:");
        Optional<String> em = d2.showAndWait();
        if (em.isEmpty() || em.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog d3 = new TextInputDialog();
        d3.setTitle("Add User");
        d3.setHeaderText(null);
        d3.setContentText("Password:");
        Optional<String> pw = d3.showAndWait();
        if (pw.isEmpty() || pw.get().trim().isEmpty()) {
            return;
        }

        User u = new User();
        u.setUsername(un.get().trim());
        u.setEmail(em.get().trim());
        u.setPassword(pw.get().trim());
        u.setUserType(User.UserType.REGISTERED);
        u.setStatus(User.UserStatus.ACTIVE);
        u.setCreatedAt(java.time.LocalDateTime.now());
        u.setHasPlayer(false);
        userDAO.save(u);
        refreshUsersTable();

        createNotification("USER_CREATED", "New user added: " + u.getUsername() + " (" + u.getEmail() + ")");
        updateNotificationBadge();
    }

    @FXML
    private void handleEditUser(ActionEvent e) {
        if (usersTable == null) return;
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a user first.");
            return;
        }

        TextInputDialog d = new TextInputDialog(selected.getEmail());
        d.setTitle("Edit User");
        d.setHeaderText(null);
        d.setContentText("Email:");
        Optional<String> em = d.showAndWait();
        if (em.isEmpty() || em.get().trim().isEmpty()) {
            return;
        }
        String oldEmail = selected.getEmail();
        selected.setEmail(em.get().trim());
        userDAO.update(selected);
        refreshUsersTable();

        createNotification("USER_UPDATED", "User updated: " + selected.getUsername() + " (email: " + oldEmail + " -> " + selected.getEmail() + ")");
        updateNotificationBadge();
    }

    @FXML
    private void handleDeleteUser(ActionEvent e) {
        if (usersTable == null) return;
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a user first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user '" + selected.getUsername() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            String username = selected.getUsername();
            userDAO.delete(selected);
            refreshUsersTable();

            createNotification("USER_DELETED", "User deleted: " + username);
            updateNotificationBadge();
        }
    }

    @FXML
    private void handleAddGame(ActionEvent e) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add Game");
        d.setHeaderText(null);
        d.setContentText("Game name:");
        Optional<String> name = d.showAndWait();
        if (name.isEmpty() || name.get().trim().isEmpty()) return;

        TextInputDialog d2 = new TextInputDialog();
        d2.setTitle("Add Game");
        d2.setHeaderText(null);
        d2.setContentText("Release year (optional):");
        Optional<String> yearText = d2.showAndWait();

        Game g = new Game();
        g.setName(name.get().trim());
        g.setCreatedAt(java.time.LocalDateTime.now());
        if (yearText.isPresent() && !yearText.get().trim().isEmpty()) {
            try {
                g.setReleaseYear(Short.parseShort(yearText.get().trim()));
            } catch (Exception ignored) {
            }
        }
        gameDAO.save(g);
        refreshGamesTable();

        createNotification("GAME_CREATED", "New game added: " + g.getName());
        updateNotificationBadge();
    }

    @FXML
    private void handleEditGame(ActionEvent e) {
        if (gamesTable == null) return;
        Game selected = gamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a game first.");
            return;
        }
        TextInputDialog d = new TextInputDialog(selected.getName());
        d.setTitle("Edit Game");
        d.setHeaderText(null);
        d.setContentText("Game name:");
        Optional<String> name = d.showAndWait();
        if (name.isEmpty() || name.get().trim().isEmpty()) return;
        String oldName = selected.getName();
        selected.setName(name.get().trim());
        gameDAO.update(selected);
        refreshGamesTable();

        createNotification("GAME_UPDATED", "Game updated: " + oldName + " -> " + selected.getName());
        updateNotificationBadge();
    }

    @FXML
    private void handleDeleteGame(ActionEvent e) {
        if (gamesTable == null) return;
        Game selected = gamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a game first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete game '" + selected.getName() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            String name = selected.getName();
            gameDAO.delete(selected);
            refreshGamesTable();

            createNotification("GAME_DELETED", "Game deleted: " + name);
            updateNotificationBadge();
        }
    }

    @FXML
    private void handleAddTeam(ActionEvent e) {
        List<Game> games = gameDAO.findAll();
        if (games == null || games.isEmpty()) {
            showNotImplemented("Add a game first.");
            return;
        }
        Game game = games.get(0);
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Create Team");
        d.setHeaderText(null);
        d.setContentText("Team name:");
        Optional<String> name = d.showAndWait();
        if (name.isEmpty() || name.get().trim().isEmpty()) return;

        Team t = new Team();
        t.setName(name.get().trim());
        t.setGame(game);
        t.setCreatedAt(java.time.LocalDateTime.now());
        teamDAO.save(t);
        refreshTeamsTable();

        createNotification("TEAM_CREATED", "New team added: " + t.getName());
        updateNotificationBadge();
    }

    @FXML
    private void handleEditTeam(ActionEvent e) {
        if (teamsTable == null) return;
        Team selected = teamsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a team first.");
            return;
        }
        TextInputDialog d = new TextInputDialog(selected.getName());
        d.setTitle("Edit Team");
        d.setHeaderText(null);
        d.setContentText("Team name:");
        Optional<String> name = d.showAndWait();
        if (name.isEmpty() || name.get().trim().isEmpty()) return;
        String oldName = selected.getName();
        selected.setName(name.get().trim());
        teamDAO.update(selected);
        refreshTeamsTable();

        createNotification("TEAM_UPDATED", "Team updated: " + oldName + " -> " + selected.getName());
        updateNotificationBadge();
    }

    @FXML
    private void handleDeleteTeam(ActionEvent e) {
        if (teamsTable == null) return;
        Team selected = teamsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a team first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete team '" + selected.getName() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            String name = selected.getName();
            teamDAO.delete(selected);
            refreshTeamsTable();

            createNotification("TEAM_DELETED", "Team deleted: " + name);
            updateNotificationBadge();
        }
    }

    @FXML
    private void handleAddPlayer(ActionEvent e) {
        List<Game> games = gameDAO.findAll();
        if (games == null || games.isEmpty()) {
            showNotImplemented("Add a game first.");
            return;
        }
        Game game = games.get(0);

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add Player");
        d.setHeaderText(null);
        d.setContentText("Nickname:");
        Optional<String> nick = d.showAndWait();
        if (nick.isEmpty() || nick.get().trim().isEmpty()) return;

        Player p = new Player();
        p.setNickname(nick.get().trim());
        p.setGame(game);
        p.setCreatedAt(java.time.LocalDateTime.now());
        playerDAO.save(p);
        refreshPlayersTable();

        createNotification("PLAYER_CREATED", "New player added: " + p.getNickname());
        updateNotificationBadge();
    }

    @FXML
    private void handleEditPlayer(ActionEvent e) {
        if (playersTable == null) return;
        Player selected = playersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a player first.");
            return;
        }
        TextInputDialog d = new TextInputDialog(selected.getNickname());
        d.setTitle("Edit Player");
        d.setHeaderText(null);
        d.setContentText("Nickname:");
        Optional<String> nick = d.showAndWait();
        if (nick.isEmpty() || nick.get().trim().isEmpty()) return;
        String oldNick = selected.getNickname();
        selected.setNickname(nick.get().trim());
        playerDAO.update(selected);
        refreshPlayersTable();

        createNotification("PLAYER_UPDATED", "Player updated: " + oldNick + " -> " + selected.getNickname());
        updateNotificationBadge();
    }

    @FXML
    private void handleDeletePlayer(ActionEvent e) {
        if (playersTable == null) return;
        Player selected = playersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotImplemented("Select a player first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete player '" + selected.getNickname() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            String nick = selected.getNickname();
            playerDAO.delete(selected);
            refreshPlayersTable();

            createNotification("PLAYER_DELETED", "Player deleted: " + nick);
            updateNotificationBadge();
        }
    }

    private void refreshUsersTable() {
        if (usersTable == null) return;
        List<User> all = userDAO.findAll();
        if (all != null) {
            usersTable.setItems(FXCollections.observableArrayList(all));
        }
    }

    private void refreshGamesTable() {
        if (gamesTable == null) return;
        List<Game> all = gameDAO.findAll();
        if (all != null) {
            gamesTable.setItems(FXCollections.observableArrayList(all));
        }
    }

    private void refreshTeamsTable() {
        if (teamsTable == null) return;
        List<Team> all = teamDAO.findAll();
        if (all != null) {
            teamsTable.setItems(FXCollections.observableArrayList(all));
        }
    }

    private void refreshPlayersTable() {
        if (playersTable == null) return;
        List<Player> all = playerDAO.findAll();
        if (all != null) {
            playersTable.setItems(FXCollections.observableArrayList(all));
        }
    }

    private void showNotImplemented(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createNotification(String type, String message) {
        try {
            System.out.println("DEBUG: Creating notification - Type: " + type + ", Message: " + message);
            User adminUser = SessionManager.getCurrentUser();
            System.out.println("DEBUG: Current user from session: " + adminUser);
            if (adminUser == null) {
                List<User> allUsers = userDAO.findAll();
                System.out.println("DEBUG: All users count: " + (allUsers != null ? allUsers.size() : 0));
                if (allUsers != null && !allUsers.isEmpty()) {
                    adminUser = allUsers.get(0);
                    System.out.println("DEBUG: Using first user: " + adminUser.getUsername());
                } else {
                    System.err.println("ERROR: No users found to associate notification with");
                    return;
                }
            }
            Notification notification = new Notification(adminUser, type, message);
            System.out.println("DEBUG: Notification object created: " + notification.getType() + " for user: " + adminUser.getUsername());
            notificationDAO.save(notification);
            System.out.println("DEBUG: Notification saved successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, loginRoot, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
            stage.setMaximized(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        if (user != null) {
            String u = user.getUsername();
            if (adminNameLabel != null) {
                adminNameLabel.setText(u);
            }
            if (u != null && !u.isEmpty()) {
                String initial = u.substring(0, 1).toUpperCase();
                if (avatarLabel != null) {
                    avatarLabel.setText(initial);
                }
            }
        }
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}
