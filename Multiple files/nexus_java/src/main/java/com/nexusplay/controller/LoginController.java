package com.nexusplay.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import com.nexusplay.MainApp;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;
import com.nexusplay.config.DatabaseConnection;
import com.nexusplay.service.FaceRecognitionService;
import com.nexusplay.service.GoogleOAuthService;
import com.nexusplay.util.PrimaryButtonEffects;
import com.nexusplay.util.SceneNavigation;
import com.nexusplay.util.SessionManager;

import java.util.prefs.Preferences;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class LoginController extends BaseController {

    private static final String PREF_NODE = "com/nexusplay/login";
    private static final String KEY_USERNAME = "saved_username";
    private static final String KEY_PASSWORD = "saved_password";
    private static final String KEY_REMEMBER = "remember_me";

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane scrollContent;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Button btnFaceLogin;
    @FXML private Hyperlink signupButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotPasswordLink;

    private FaceRecognitionService faceService;
    private Timeline autoScanTimer;
    private Timeline dotsAnim;
    private Stage facePopupStage;
    private Label popupStatusLabel;
    private ProgressIndicator popupSpinner;
    private Button popupCancelBtn;
    private volatile boolean recognitionInFlight = false;

    @FXML
    public void initialize() {
        bindWindowChrome();
        Font.loadFont(getClass().getResourceAsStream("/fonts/Rajdhani-Bold.ttf"), 14);

        PrimaryButtonEffects.installGradientPrimary(loginButton);

        scrollContent.minHeightProperty().bind(scrollPane.heightProperty());

        loadSavedCredentials();

        passwordField.setOnAction(e -> handleLogin(null));
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Platform.runLater(() -> {
            Scene scene = usernameField.getScene();
            if (scene != null) {
                MainApp.bindRootToScene(scene);
            }
        });
    }

    @FXML
    private void handleGoogleLogin() {
        // Check if Google OAuth is configured
        if (!GoogleOAuthService.isConfigured()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Google Login");
            alert.setHeaderText("Google OAuth Not Configured");
            alert.setContentText(GoogleOAuthService.getSetupInstructions());
            alert.showAndWait();
            return;
        }
        
        try {
            // Open Google OAuth URL in browser
            String authUrl = GoogleOAuthService.getAuthorizationUrl();
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authUrl));
            
            // Show waiting dialog
            Alert waitingAlert = new Alert(Alert.AlertType.INFORMATION);
            waitingAlert.setTitle("Google Login");
            waitingAlert.setHeaderText("Complete Google Sign-In");
            waitingAlert.setContentText("Please complete the sign-in in your browser, then click OK to continue.");
            waitingAlert.showAndWait();
            
            // For now, create a test user (in production, you'd handle the OAuth callback)
            UserDAO userDAO = new UserDAO();
            User googleUser = userDAO.findByUsernameOrEmail("google.user@gmail.com");
            
            if (googleUser == null) {
                googleUser = new User();
                googleUser.setUsername("google_user");
                googleUser.setEmail("google.user@gmail.com");
                googleUser.setPassword("google_oauth_123456");
                googleUser.setUserType(User.UserType.REGISTERED);
                googleUser.setStatus(User.UserStatus.ACTIVE);
                googleUser.setCreatedAt(java.time.LocalDateTime.now());
                googleUser.setHasPlayer(false);
                
                try {
                    userDAO.save(googleUser);
                    System.out.println("Created Google test user: google_user / google_oauth_123456");
                } catch (Exception e) {
                    showError("Failed to create Google test user: " + e.getMessage());
                    return;
                }
            }
            
            SessionManager.setCurrentUser(googleUser);
        
        // Navigate to appropriate dashboard based on user type
        switch (googleUser.getUserType()) {
            case ADMIN:
                navigateTo("AdminDashboard.fxml", "Admin Back Office - NexusPlay");
                break;
            case COACH:
                navigateTo("coach-dashboard.fxml", "Coach Dashboard - NexusPlay");
                break;
            case ORGANIZATION:
                navigateTo("organization-dashboard.fxml", "Organization Dashboard - NexusPlay");
                break;
            case VISITOR:
                navigateTo("visitor-dashboard.fxml", "Visitor Dashboard - NexusPlay");
                break;
            case REGISTERED:
                navigateTo("player-dashboard.fxml", "Player Dashboard - NexusPlay");
                break;
        }
        
        } catch (Exception e) {
            showError("Google login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleFaceLogin(ActionEvent event) {
        if (btnFaceLogin == null) {
            showError("Face login is not available.");
            return;
        }
        btnFaceLogin.setDisable(true);
        showFacePopup();
    }

    private void showFacePopup() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("face-popup");
        content.setPrefWidth(280);

        Label icon = new Label("📷");
        icon.setStyle("-fx-font-size: 36px;");

        Label title = new Label("Face Recognition");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        popupSpinner = new ProgressIndicator(-1);
        popupSpinner.setPrefSize(52, 52);
        popupSpinner.setStyle("-fx-progress-color: #FF4D2E;");

        popupStatusLabel = new Label("Initializing camera...");
        popupStatusLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.60); -fx-font-size: 13px;");
        popupStatusLabel.setWrapText(true);
        popupStatusLabel.setAlignment(Pos.CENTER);
        popupStatusLabel.setMaxWidth(220);

        Label dotsLabel = new Label("●  ●  ●");
        dotsLabel.setStyle("-fx-text-fill: rgba(255,77,46,0.50); -fx-font-size: 11px;");
        dotsAnim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(dotsLabel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(dotsLabel.opacityProperty(), 0.2))
        );
        dotsAnim.setCycleCount(Timeline.INDEFINITE);
        dotsAnim.setAutoReverse(true);
        dotsAnim.play();

        popupCancelBtn = new Button("Cancel");
        popupCancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: rgba(255,255,255,0.18);" +
                        "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
                        "-fx-text-fill: rgba(255,255,255,0.55);" +
                        "-fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-padding: 8 28;"
        );
        popupCancelBtn.setVisible(false);
        popupCancelBtn.setOnAction(e -> closeFacePopup());
        popupCancelBtn.setOnMouseEntered(e ->
                popupCancelBtn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.07);" +
                                "-fx-border-color: rgba(255,255,255,0.35);" +
                                "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
                                "-fx-text-fill: white; -fx-font-size: 13px;" +
                                "-fx-cursor: hand; -fx-padding: 8 28;"
                )
        );

        content.getChildren().addAll(icon, title, popupSpinner, popupStatusLabel, dotsLabel, popupCancelBtn);

        new Timeline(new KeyFrame(Duration.seconds(4), e -> popupCancelBtn.setVisible(true))).play();

        facePopupStage = new Stage();
        facePopupStage.initStyle(StageStyle.TRANSPARENT);
        facePopupStage.initModality(Modality.NONE);
        facePopupStage.initOwner(btnFaceLogin.getScene().getWindow());

        Scene popupScene = new Scene(content);
        popupScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popupScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        facePopupStage.setScene(popupScene);

        Bounds bounds = btnFaceLogin.localToScreen(btnFaceLogin.getBoundsInLocal());
        facePopupStage.setX(bounds.getCenterX() - 140);
        facePopupStage.setY(bounds.getMinY() - 340);

        facePopupStage.setOnHidden(e -> closeFacePopup());
        facePopupStage.show();

        startFaceRecognition();
    }

    private void startFaceRecognition() {
        try {
            faceService = new FaceRecognitionService();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (popupStatusLabel != null) {
                popupStatusLabel.setText("Camera init failed: " + ex.getMessage());
            }
            return;
        }

        faceService.startCamera(
                image -> {
                    // no preview in popup
                },
                status -> {
                    if (popupStatusLabel != null) {
                        popupStatusLabel.setText(status);
                    }
                }
        );

        autoScanTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> attemptRecognition()));
        autoScanTimer.setCycleCount(Timeline.INDEFINITE);
        autoScanTimer.play();
    }

    private void attemptRecognition() {
        if (recognitionInFlight) {
            return;
        }
        recognitionInFlight = true;

        if (popupStatusLabel != null) {
            popupStatusLabel.setText("Scanning your face...");
        }

        FaceRecognitionService svc = faceService;
        if (svc == null) {
            recognitionInFlight = false;
            return;
        }

        new Thread(() -> {
            User matched = svc.recognizeFace(UserDAO.getUsersWithPhotos());
            Platform.runLater(() -> {
                try {
                    if (matched != null) {
                        if (autoScanTimer != null) {
                            autoScanTimer.stop();
                        }
                        if (faceService != null) {
                            faceService.stopCamera();
                        }

                        if (popupSpinner != null) {
                            popupSpinner.setVisible(false);
                        }
                        if (popupStatusLabel != null) {
                            popupStatusLabel.setText("✓ Welcome, " + matched.getUsername() + "!");
                            popupStatusLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 14px; -fx-font-weight: bold;");
                        }

                        new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                            closeFacePopup();
                            SessionManager.setCurrentUser(matched);
                            switch (matched.getUserType()) {
                                case ADMIN:
                                    navigateTo("AdminDashboard.fxml", "Admin Back Office - NexusPlay");
                                    break;
                                case COACH:
                                    navigateTo("coach-dashboard.fxml", "Coach Dashboard - NexusPlay");
                                    break;
                                case ORGANIZATION:
                                    navigateTo("organization-dashboard.fxml", "Organization Dashboard - NexusPlay");
                                    break;
                                case VISITOR:
                                    navigateTo("visitor-dashboard.fxml", "Visitor Dashboard - NexusPlay");
                                    break;
                                case REGISTERED:
                                default:
                                    navigateTo("player-dashboard.fxml", "Player Dashboard - NexusPlay");
                                    break;
                            }
                        })).play();
                    } else {
                        if (popupStatusLabel != null) {
                            popupStatusLabel.setText("No match found. Retrying...");
                        }
                    }
                } finally {
                    recognitionInFlight = false;
                }
            });
        }, "face-recog-thread").start();
    }

    private void closeFacePopup() {
        recognitionInFlight = false;

        if (autoScanTimer != null) {
            autoScanTimer.stop();
            autoScanTimer = null;
        }
        if (dotsAnim != null) {
            dotsAnim.stop();
            dotsAnim = null;
        }
        if (faceService != null) {
            faceService.stopCamera();
            faceService = null;
        }

        Stage s = facePopupStage;
        facePopupStage = null;
        if (s != null) {
            s.close();
        }

        popupStatusLabel = null;
        popupSpinner = null;
        popupCancelBtn = null;

        if (btnFaceLogin != null) {
            btnFaceLogin.setDisable(false);
        }
    }

    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        if (!remember) {
            return;
        }

        String savedUsername = prefs.get(KEY_USERNAME, "");
        String savedPassword = prefs.get(KEY_PASSWORD, "");

        if (usernameField != null) {
            usernameField.setText(savedUsername);
        }
        if (passwordField != null) {
            passwordField.setText(savedPassword);
        }
        if (rememberMeCheckbox != null) {
            rememberMeCheckbox.setSelected(true);
        }
    }

    private void saveOrClearCredentials(String username, String password) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);

        if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
            prefs.put(KEY_USERNAME, username);
            prefs.put(KEY_PASSWORD, password);
            prefs.putBoolean(KEY_REMEMBER, true);
        } else {
            prefs.remove(KEY_USERNAME);
            prefs.remove(KEY_PASSWORD);
            prefs.putBoolean(KEY_REMEMBER, false);
        }
        
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error flushing preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }

        if (username.length() < 4) {
            showError("Username must be at least 4 characters.");
            return;
        }

        if (username.length() > 10000) {
            showError("Username must be less than 10000 characters.");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            return;
        }

        if (password.length() > 10000) {
            showError("Password must be less than 10000 characters.");
            return;
        }

        User user = UserDAO.login(username, password);

        System.out.println("=== CONTROLLER DEBUG ===");
        System.out.println("UserDAO returned: " + user);
        if (user != null) {
            System.out.println("user_type value: '" + user.getUserType() + "'");
            System.out.println("toUpperCase: '" + user.getUserType().name().toUpperCase() + "'");
        }

        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Username entered: " + username);
        System.out.println("User found: " + (user != null));
        if (user != null) {
            System.out.println("user_type from DB: '" + user.getUserType() + "'");
        }

        if (user == null) {
            String dbError = DatabaseConnection.getLastError();
            if (dbError != null && !dbError.isBlank()) {
                showError("Database connection failed. Check DB credentials/config.");
                return;
            }
            showError("Invalid username or password.");
            Preferences prefs = Preferences.userRoot().node(PREF_NODE);
            if (prefs.getBoolean(KEY_REMEMBER, false)) {
                prefs.putBoolean(KEY_REMEMBER, false);
            }
            return;
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            showError("Your account is not active. Please contact support.");
            return;
        }

        saveOrClearCredentials(username, password);
        SessionManager.setCurrentUser(user);

        System.out.println("Logged in as: " + username + " | Type: " + user.getUserType().name());

        switch (user.getUserType()) {
            case ADMIN:
                // Same resource root as MainApp: /fxml/... (see src/main/resources/fxml/)
                navigateTo("AdminDashboard.fxml", "Admin Back Office - NexusPlay");
                break;
            case COACH:
            case ORGANIZATION:
            case VISITOR:
            case REGISTERED:
            default:
                navigateTo("Home.fxml", "NexusPlay - Home");
                break;
        }
    }

    /**
     * Resolves FXML under {@code /fxml/} the same way as {@link MainApp} loads login
     * (classpath root = {@code src/main/resources}).
     */
    private static java.net.URL resolveFxmlUrl(String fileName) {
        String name = fileName == null ? "" : fileName.trim();
        if (name.isEmpty()) {
            return null;
        }
        if (name.startsWith("fxml/")) {
            name = name.substring("fxml/".length());
        }
        String path = "/fxml/" + name;
        java.net.URL url = MainApp.class.getResource(path);
        if (url == null) {
            url = LoginController.class.getResource(path);
        }
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(path.substring(1));
        }
        return url;
    }

    private void navigateTo(String fxmlFileName, String windowTitle) {
        try {
            Node rootNode = usernameField.getScene().getRoot();
            FadeTransition fade = new FadeTransition(Duration.millis(200), rootNode);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                try {
                    java.net.URL url = resolveFxmlUrl(fxmlFileName);
                    System.out.println("[navigateTo] FXML file: " + fxmlFileName + " -> URL: " + url);
                    if (url == null) {
                        System.out.println("  (tried MainApp.class /fxml/" + fxmlFileName.replaceFirst("^fxml/", "") + ")");
                        showError("Failed to load screen: /fxml/" + fxmlFileName.replaceFirst("^fxml/", ""));
                        rootNode.setOpacity(1);
                        return;
                    }
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();

                    Object controller = loader.getController();
                    if (controller instanceof DashboardController) {
                        ((DashboardController) controller).setCurrentUser(SessionManager.getCurrentUser());
                    }

                    Scene scene = usernameField.getScene();
                    scene.setRoot(root);
                    SceneNavigation.applyTheme(scene);
                    MainApp.bindRootToScene(scene);

                    Stage stage = (Stage) scene.getWindow();
                    stage.setTitle(windowTitle);
                    stage.setMaximized(true);

                    root.setOpacity(0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Throwable cause = ex.getCause();
                    while (cause != null) {
                        System.out.println("CAUSED BY: " + cause.getMessage());
                        cause = cause.getCause();
                    }
                    errorLabel.setText("Failed: " + ex.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #FF4D2E; -fx-font-size: 13px;");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    rootNode.setOpacity(1);
                }
            });
            fade.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateAccount.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signupButton.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root);
        } catch (Exception e) {
            showError("Failed to open signup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForgotPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root);
        } catch (Exception e) {
            showError("Failed to open password reset.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #FF4D2E; -fx-font-size: 13px;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
