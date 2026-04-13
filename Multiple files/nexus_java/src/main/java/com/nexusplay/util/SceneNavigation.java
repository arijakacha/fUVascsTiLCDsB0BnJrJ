package com.nexusplay.util;

import javafx.animation.FadeTransition;
import com.nexusplay.MainApp;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;

public final class SceneNavigation {

    public static final double DEFAULT_WIDTH = 1000;
    public static final double DEFAULT_HEIGHT = 700;

    private static final String STYLES_GLOBAL = "/css/styles.css";
    private static final String STYLES_THEME  = "/css/nexus-theme.css";

    private SceneNavigation() {}

    /**
     * Full clear + re-add. Use only when creating a brand-new Scene.
     * For existing scenes, prefer {@link #ensureTheme(Scene)} to avoid the white-flash
     * caused by {@code clear()} briefly leaving the scene without any CSS.
     */
    public static void applyTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        scene.getStylesheets().clear();
        addStylesheets(scene);
        scene.setFill(Color.TRANSPARENT);
    }

    /**
     * Adds missing stylesheets without clearing existing ones — no white flash.
     */
    private static void ensureTheme(Scene scene) {
        if (scene == null) {
            return;
        }
        addStylesheets(scene);
        scene.setFill(Color.TRANSPARENT);
    }

    private static void addStylesheets(Scene scene) {
        List<String> sheets = scene.getStylesheets();
        var urlGlobal = SceneNavigation.class.getResource(STYLES_GLOBAL);
        var urlTheme  = SceneNavigation.class.getResource(STYLES_THEME);
        if (urlGlobal != null) {
            String ext = urlGlobal.toExternalForm();
            if (!sheets.contains(ext)) {
                sheets.add(ext);
            }
        }
        if (urlTheme != null) {
            String ext = urlTheme.toExternalForm();
            if (!sheets.contains(ext)) {
                sheets.add(ext);
            }
        }
    }

    public static void replaceSceneContent(Stage stage, Parent newRoot) {
        replaceSceneContent(stage, newRoot, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Swaps the scene root with a short fade.
     * Stage dimensions are preserved when the window is filling the screen so there
     * is no "shrink then re-expand" flash during navigation or logout.
     */
    public static void replaceSceneContent(Stage stage, Parent newRoot, double w, double h) {
        try {
            if (stage == null) {
                System.err.println("SceneNavigation: stage is null");
                return;
            }
            if (newRoot == null) {
                System.err.println("SceneNavigation: newRoot is null");
                return;
            }

            Scene scene = stage.getScene();

            if (scene == null) {
                // First-time scene setup
                Scene newScene = new Scene(newRoot);
                applyTheme(newScene);
                MainApp.bindRootToScene(newScene);
                stage.setScene(newScene);
                stage.setWidth(w);
                stage.setHeight(h);
                return;
            }

            // ── Stage sizing ──────────────────────────────────────────────────────────
            try {
                if (StageLayout.isStageFillingScreen(stage)) {
                    StageLayout.applyFillBoundsNow(stage);
                }
            } catch (Exception e) {
                System.err.println("SceneNavigation: Error applying fill bounds: " + e.getMessage());
            }

            // ── Direct scene replacement (no fade transition to prevent mouse event conflicts) ──
            try {
                scene.setRoot(newRoot);
                ensureTheme(scene);
                MainApp.bindRootToScene(scene);
            } catch (Exception ex) {
                System.err.println("SceneNavigation: Error setting scene root: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("SceneNavigation: Error in replaceSceneContent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** @deprecated use {@link MainApp#bindRootToScene(Scene)} */
    @Deprecated
    public static void bindRegionToScene(Scene scene, Parent root) {
        MainApp.bindRootToScene(scene);
    }

    private static void fadeIn(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}
