package com.nexusplay.util;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Cross-platform stage sizing: {@link Stage#setFullScreen(boolean)} is unreliable for
 * {@link javafx.stage.StageStyle#UNDECORATED} windows on macOS; aligning the stage to the
 * primary screen's visual bounds ({@link Screen#getPrimary()} / {@link Screen#getVisualBounds()})
 * matches the usable area (menu bar / dock aware) on any OS.
 */
public final class StageLayout {

    private static final double EPS = 10.0;

    private static final double WINDOWED_WIDTH = 1080;
    private static final double WINDOWED_HEIGHT = 720;

    private StageLayout() {}

    public static Rectangle2D primaryVisualBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    /**
     * Places the stage over the primary screen's usable rectangle (excludes menu bar / dock where applicable).
     * After minimize/deiconify on macOS, bounds sometimes need a second application on the next layout pulse.
     */
    public static void fitStageToScreen(Stage stage) {
        if (stage == null) {
            return;
        }
        Runnable apply = () -> {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            applyVisualBounds(stage);
            // Re-apply once more if the window manager ignored the first pass (common after dock restore).
            Platform.runLater(() -> {
                if (stage.isIconified()) {
                    return;
                }
                if (!geometryMatchesVisualBounds(stage)) {
                    applyVisualBounds(stage);
                }
            });
        };
        if (Platform.isFxApplicationThread()) {
            apply.run();
        } else {
            Platform.runLater(apply);
        }
    }

    private static void applyVisualBounds(Stage stage) {
        Rectangle2D vb = primaryVisualBounds();
        stage.setMaximized(false);
        stage.setX(vb.getMinX());
        stage.setY(vb.getMinY());
        stage.setWidth(vb.getWidth());
        stage.setHeight(vb.getHeight());
    }

    /**
     * Applies fill bounds on the FX thread immediately, without extra {@link Platform#runLater} pulses.
     * Use when swapping scene roots so the window does not briefly appear at a default/smaller size.
     */
    public static void applyFillBoundsNow(Stage stage) {
        if (stage == null) {
            return;
        }
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> applyFillBoundsNow(stage));
            return;
        }
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
        applyVisualBounds(stage);
    }

    private static boolean geometryMatchesVisualBounds(Stage stage) {
        Rectangle2D vb = primaryVisualBounds();
        return Math.abs(stage.getX() - vb.getMinX()) < EPS
                && Math.abs(stage.getY() - vb.getMinY()) < EPS
                && Math.abs(stage.getWidth() - vb.getWidth()) < EPS
                && Math.abs(stage.getHeight() - vb.getHeight()) < EPS;
    }

    /**
     * True when the stage matches the primary visual bounds (within tolerance).
     * Does not use {@link Stage#isMaximized()}: on macOS with undecorated stages it is often wrong after
     * minimize/restore, which made the maximize button toggle the wrong way.
     */
    public static boolean isStageFillingScreen(Stage stage) {
        if (stage == null || stage.isIconified()) {
            return false;
        }
        return geometryMatchesVisualBounds(stage);
    }

    /** Centered window with a comfortable default size, clamped to the visual bounds. */
    public static void restoreStageWindowed(Stage stage) {
        if (stage == null) {
            return;
        }
        Platform.runLater(() -> {
            stage.setMaximized(false);
            Rectangle2D vb = primaryVisualBounds();
            double minW = stage.getMinWidth() > 0 ? stage.getMinWidth() : 860;
            double minH = stage.getMinHeight() > 0 ? stage.getMinHeight() : 600;
            double w = Math.min(WINDOWED_WIDTH, vb.getWidth() - 32);
            double h = Math.min(WINDOWED_HEIGHT, vb.getHeight() - 32);
            w = Math.max(w, minW);
            h = Math.max(h, minH);
            if (w > vb.getWidth()) {
                w = vb.getWidth() - 16;
            }
            if (h > vb.getHeight()) {
                h = vb.getHeight() - 16;
            }
            stage.setWidth(w);
            stage.setHeight(h);
            stage.setX(vb.getMinX() + (vb.getWidth() - w) / 2.0);
            stage.setY(vb.getMinY() + (vb.getHeight() - h) / 2.0);
        });
    }
}
