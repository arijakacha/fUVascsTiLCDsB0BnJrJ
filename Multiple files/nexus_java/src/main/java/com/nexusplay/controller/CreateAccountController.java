package com.nexusplay.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;




import com.nexusplay.MainApp;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;
import com.nexusplay.util.PrimaryButtonEffects;
import com.nexusplay.util.SceneNavigation;

import java.time.LocalDateTime;

public class CreateAccountController extends BaseController {

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane scrollContent;

    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox confirmPasswordBlock;
    @FXML private Button createAccountButton;
    @FXML private Hyperlink signInLink;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        bindWindowChrome();
        Font.loadFont(getClass().getResourceAsStream("/fonts/Rajdhani-Bold.ttf"), 14);
        roleComboBox.getItems().addAll("Player", "Organizer", "Coach");
        roleComboBox.setValue("Player");
        PrimaryButtonEffects.installGradientPrimary(createAccountButton);

        scrollContent.minHeightProperty().bind(scrollPane.heightProperty());

        Platform.runLater(() -> {
            Scene scene = scrollPane.getScene();
            if (scene != null) {
                MainApp.bindRootToScene(scene);
            }
        });

        TextInputControl[] inputs = {
                fullNameField, emailField, usernameField, passwordField, confirmPasswordField
        };
        for (TextInputControl f : inputs) {
            f.focusedProperty().addListener((o, ov, nv) -> {
                if (Boolean.TRUE.equals(nv)) {
                    f.getStyleClass().removeAll("field-error");
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                }
            });
        }
    }

    @FXML
    private void handleCreateAccount() {
        clearErrors();
        String fn = fullNameField.getText().trim();
        String em = emailField.getText().trim();
        String un = usernameField.getText().trim();
        String pw = passwordField.getText();
        String cpw = confirmPasswordField.getText();
        boolean ok = true;
        if (fn.isEmpty()) {
            markField(fullNameField);
            ok = false;
        }
        if (fn.length() < 4) {
            markField(fullNameField);
            ok = false;
        }
        if (fn.length() > 10000) {
            markField(fullNameField);
            ok = false;
        }
        if (em.isEmpty()) {
            markField(emailField);
            ok = false;
        }
        if (!em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            markField(emailField);
            ok = false;
        }
        if (em.length() > 10000) {
            markField(emailField);
            ok = false;
        }
        if (un.isEmpty()) {
            markField(usernameField);
            ok = false;
        }
        if (un.length() < 4) {
            markField(usernameField);
            ok = false;
        }
        if (un.length() > 10000) {
            markField(usernameField);
            ok = false;
        }
        if (pw.isEmpty()) {
            markField(passwordField);
            ok = false;
        }
        if (pw.length() < 4) {
            markField(passwordField);
            ok = false;
        }
        if (pw.length() > 10000) {
            markField(passwordField);
            ok = false;
        }
        if (cpw.isEmpty()) {
            markField(confirmPasswordField);
            ok = false;
        }
        if (!ok) {
            showError("Please fill in all fields with valid data (min 4 chars, max 10000 chars, valid email format).");
            return;
        }
        if (!pw.equals(cpw)) {
            confirmPasswordField.getStyleClass().add("field-error");
            shake(confirmPasswordBlock);
            showError("Passwords do not match.");
            return;
        }

        // Check if username or email already exists
        UserDAO userDAO = new UserDAO();
        if (userDAO.findByUsername(un) != null) {
            markField(usernameField);
            showError("Username already exists.");
            return;
        }
        if (userDAO.findByUsernameOrEmail(em) != null) {
            markField(emailField);
            showError("Email already registered.");
            return;
        }

        // Create new user
        User user = new User();
        user.setUsername(un);
        user.setEmail(em);
        user.setPassword(pw);
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setHasPlayer(false);

        // Set user type based on selection
        String role = roleComboBox.getValue();
        switch (role) {
            case "Organizer":
                user.setUserType(User.UserType.ORGANIZATION);
                break;
            case "Coach":
                user.setUserType(User.UserType.COACH);
                break;
            case "Player":
            default:
                user.setUserType(User.UserType.REGISTERED);
                break;
        }

        try {
            System.out.println("Saving user: " + user.getUsername());
            userDAO.save(user);
            System.out.println("User saved successfully");
            showSuccessPopup("Account created successfully! Redirecting to login...");
            createAccountButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        createAccountButton.setDisable(false);
                        navigateToLogin();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating account: " + e.getMessage());
            showError("Failed to create account: " + e.getMessage());
        }
    }

    private static void markField(TextInputControl c) {
        if (!c.getStyleClass().contains("field-error")) {
            c.getStyleClass().add("field-error");
        }
    }

    private void clearErrors() {
        for (TextInputControl c : new TextInputControl[]{
                fullNameField, emailField, usernameField, passwordField, confirmPasswordField
        }) {
            c.getStyleClass().removeAll("field-error");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccessPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

    private void shake(Node node) {
        TranslateTransition t = new TranslateTransition(Duration.millis(45), node);
        t.setFromX(0);
        t.setToX(12);
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.setOnFinished(e -> node.setTranslateX(0));
        t.play();
    }

    @FXML
    private void handleGoogle() {
        // Simulate Google OAuth account creation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Google Account Creation");
        alert.setHeaderText("Google OAuth Simulation");
        alert.setContentText("Google OAuth is not configured. This would normally open Google's OAuth flow.\n\nFor demo purposes, creating a test Google user account...");
        alert.showAndWait();
        
        // Create or find a test Google user
        UserDAO userDAO = new UserDAO();
        User googleUser = userDAO.findByUsernameOrEmail("google.user@gmail.com");
        
        if (googleUser == null) {
            // Create a new Google user
            googleUser = new User();
            googleUser.setUsername("google_user");
            googleUser.setEmail("google.user@gmail.com");
            googleUser.setPassword("google_oauth_123456");
            googleUser.setUserType(User.UserType.REGISTERED);
            googleUser.setStatus(User.UserStatus.ACTIVE);
            googleUser.setCreatedAt(LocalDateTime.now());
            googleUser.setHasPlayer(false);
            
            try {
                userDAO.save(googleUser);
                System.out.println("Created Google test user: google_user / google_oauth_123456");
            } catch (Exception e) {
                showError("Failed to create Google test user: " + e.getMessage());
                return;
            }
        }
        
        showSuccessPopup("Google account created successfully! Redirecting to login...");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> navigateToLogin());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleSignIn() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signInLink.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
