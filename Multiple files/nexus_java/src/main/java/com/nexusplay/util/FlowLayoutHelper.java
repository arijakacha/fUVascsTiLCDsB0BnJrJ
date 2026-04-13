package com.nexusplay.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

/**
 * Keeps {@link FlowPane} reflow aligned with the usable content width when the stage is resized.
 */
public final class FlowLayoutHelper {

    private FlowLayoutHelper() {}

    /**
     * @param widthSource region whose width drives wrapping (usually the inner content {@code VBox})
     * @param subtractPadding total horizontal inset to subtract (padding + margins), e.g. 80–96
     */
    public static void bindPrefWrapLengthToRegion(Region widthSource, FlowPane flowPane, double subtractPadding) {
        if (widthSource == null || flowPane == null) {
            return;
        }
        ChangeListener<Number> sync = (obs, oldW, newW) -> {
            double w = newW.doubleValue();
            if (w > 0) {
                flowPane.setPrefWrapLength(Math.max(420, w - subtractPadding));
            }
        };
        widthSource.widthProperty().addListener(sync);
        Platform.runLater(() -> sync.changed(widthSource.widthProperty(), 0, widthSource.getWidth()));
    }
}
