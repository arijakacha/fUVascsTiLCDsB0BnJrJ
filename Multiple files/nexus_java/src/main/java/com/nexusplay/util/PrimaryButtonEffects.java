package com.nexusplay.util;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public final class PrimaryButtonEffects {

    private PrimaryButtonEffects() {}

    /**
     * Hover: scale 1.03 (150ms). Pressed: scale 0.98. Glow via CSS (.btn-primary:hover).
     */
    public static void installGradientPrimary(Button button) {
        button.setOpacity(1.0);
        button.setScaleX(1.0);
        button.setScaleY(1.0);

        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        button.setOnMousePressed(e -> {
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });
        button.setOnMouseReleased(e -> {
            boolean h = button.isHover();
            button.setScaleX(h ? 1.03 : 1.0);
            button.setScaleY(h ? 1.03 : 1.0);
        });
    }
}
