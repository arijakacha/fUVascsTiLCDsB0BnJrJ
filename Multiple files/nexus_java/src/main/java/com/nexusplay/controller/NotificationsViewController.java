package com.nexusplay.controller;

import com.nexusplay.MainApp;
import com.nexusplay.dao.NotificationDAO;
import com.nexusplay.entity.Notification;
import com.nexusplay.util.SceneNavigation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsViewController extends BaseController {

    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, Number> idColumn;
    @FXML private TableColumn<Notification, String> typeColumn;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, String> createdAtColumn;
    @FXML private TableColumn<Notification, String> isReadColumn;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        bindWindowChrome();
        Platform.runLater(() -> {
            Scene scene = notificationsTable.getScene();
            if (scene != null) {
                MainApp.bindRootToScene(scene);
            }
        });

        configureTable();
        loadNotifications();
    }

    private void configureTable() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        }
        if (typeColumn != null) {
            typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        }
        if (messageColumn != null) {
            messageColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMessage()));
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(c -> {
                if (c.getValue().getCreatedAt() != null) {
                    return new SimpleStringProperty(c.getValue().getCreatedAt().format(formatter));
                }
                return new SimpleStringProperty("");
            });
        }
        if (isReadColumn != null) {
            isReadColumn.setCellValueFactory(c -> {
                boolean isRead = c.getValue().getIsRead() != null && c.getValue().getIsRead();
                return new SimpleStringProperty(isRead ? "Read" : "Unread");
            });
        }
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationDAO.findAll();
        if (notifications != null) {
            ObservableList<Notification> data = FXCollections.observableArrayList(notifications);
            notificationsTable.setItems(data);
        }
    }

    @FXML
    private void handleMarkAllAsRead(ActionEvent e) {
        try {
            notificationDAO.markAllAsRead(null);
            loadNotifications();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) notificationsTable.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root, SceneNavigation.DEFAULT_WIDTH, SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
