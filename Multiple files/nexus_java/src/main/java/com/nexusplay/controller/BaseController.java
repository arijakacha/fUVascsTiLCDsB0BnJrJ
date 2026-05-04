package com.nexusplay.controller;

import com.nexusplay.MainApp;
import com.nexusplay.util.StageLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 * 
 * 
 * Shared undecorated-window controls: drag title bar, maximize/fill screen, minimize, close.
 */
public abstract class BaseController {

    private double xOffset;
    private double yOffset;

    @FXML protected Button btnFullscreen;
    @FXML protected Button btnMinimize;
    @FXML protected Button btnClose;
    @FXML protected HBox titleBar;

    /** Call from each subclass {@code initialize()} after FXML injection. */
    protected void bindWindowChrome() {
        // Disabled to prevent JavaFX platform-level crashes in GlassViewEventHandler
        // The window chrome buttons (fullscreen, minimize, close) may be causing
        // mouse event conflicts at the JavaFX platform level
    }

    /**
     * @deprecated use {@link MainApp#bindRootToScene(Scene)}
     */
    @Deprecated
    protected void bindRootToScene(Scene scene, javafx.scene.layout.Region root) {
        MainApp.bindRootToScene(scene);
    }

    private void syncMaximizeButton(boolean filled) {
        if (btnFullscreen == null) {
            return;
        }
        if (filled) {
            btnFullscreen.setText("⊡");
            if (!btnFullscreen.getStyleClass().contains("is-fullscreen")) {
                btnFullscreen.getStyleClass().add("is-fullscreen");
            }
        } else {
            btnFullscreen.setText("⛶");
            btnFullscreen.getStyleClass().remove("is-fullscreen");
        }
    }

    @FXML
    protected void handleTitleBarPressed(MouseEvent event) {
        // Disabled to prevent JavaFX platform-level crashes
    }

    @FXML
    protected void handleTitleBarDragged(MouseEvent event) {
        // Disabled to prevent JavaFX platform-level crashes
    }

    @FXML
    protected void toggleFullscreen(ActionEvent event) {
        // Disabled to prevent JavaFX platform-level crashes
    }

    @FXML
    protected void minimizeWindow(ActionEvent event) {
        // Disabled to prevent JavaFX platform-level crashes
    }

    @FXML
    protected void closeWindow(ActionEvent event) {
        // Disabled to prevent JavaFX platform-level crashes
    }

    protected Stage getStage(MouseEvent event) {
        try {
            return getStageFromNode((Node) event.getSource());
        } catch (Exception e) {
            System.err.println("Error getting stage from MouseEvent: " + e.getMessage());
            return null;
        }
    }

    protected Stage getStage(ActionEvent event) {
        try {
            return getStageFromNode((Node) event.getSource());
        } catch (Exception e) {
            System.err.println("Error getting stage from ActionEvent: " + e.getMessage());
            return null;
        }
    }

    protected static Stage getStageFromNode(Node node) {
        if (node == null || node.getScene() == null) {
            return null;
        }
        return (Stage) node.getScene().getWindow();
    }
}
