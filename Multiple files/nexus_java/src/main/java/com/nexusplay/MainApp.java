package com.nexusplay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.nexusplay.util.SceneNavigation;
import com.nexusplay.util.DataInitializer;
import com.nexusplay.util.StageLayout;

import java.io.IOException;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        SceneNavigation.applyTheme(scene);

        if (root instanceof Region) {
            Region region = (Region) root;
            region.prefWidthProperty().bind(scene.widthProperty());
            region.prefHeightProperty().bind(scene.heightProperty());
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        root.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 4);");

        stage.setTitle("NexusPlay - Gaming Platform");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setWidth(1080);
        stage.setHeight(720);
        stage.setMinWidth(860);
        stage.setMinHeight(600);
        stage.show();
        // Undecorated + macOS: use visual bounds instead of setFullScreen (often ineffective)
        StageLayout.fitStageToScreen(stage);

        // Initialize sample data (professional games and products with images)
        DataInitializer.initializeSampleData();
    }

    /**
     * Call after every scene root change so layout tracks scene/stage size (resize + fullscreen).
     */
    public static void bindRootToScene(Scene scene) {
        if (scene == null) {
            return;
        }
        Parent root = scene.getRoot();
        if (root instanceof Region) {
            Region region = (Region) root;
            if (region.prefWidthProperty().isBound()) {
                region.prefWidthProperty().unbind();
            }
            if (region.prefHeightProperty().isBound()) {
                region.prefHeightProperty().unbind();
            }
            region.prefWidthProperty().bind(scene.widthProperty());
            region.prefHeightProperty().bind(scene.heightProperty());
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
