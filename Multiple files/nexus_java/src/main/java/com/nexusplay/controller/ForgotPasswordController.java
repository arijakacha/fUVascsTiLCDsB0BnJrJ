package com.nexusplay.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;



import com.nexusplay.MainApp;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;
import com.nexusplay.service.EmailService;
import com.nexusplay.util.PrimaryButtonEffects;
import com.nexusplay.util.SceneNavigation;

import java.util.Random;
import java.util.function.UnaryOperator;

public class ForgotPasswordController extends BaseController {

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane scrollContent;
    @FXML private StackPane rootPane;
    @FXML private StackPane stepStack;
    @FXML private StackPane step3Inner;

    @FXML private VBox step1;
    @FXML private VBox step2;
    @FXML private VBox step3;
    @FXML private TextField emailField;
    @FXML private Text step2Subtitle;
    @FXML private TextField digit1;
    @FXML private TextField digit2;
    @FXML private TextField digit3;
    @FXML private TextField digit4;
    @FXML private TextField digit5;
    @FXML private TextField digit6;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Button sendCodeButton;
    @FXML private Button verifyCodeButton;
    @FXML private Button resetPasswordButton;
    @FXML private VBox step3Form;
    @FXML private VBox successPane;
    @FXML private Text successCheck;

    // Missing fields from FXML
    @FXML private Hyperlink backToLoginLink1;
    @FXML private Hyperlink backToLoginLink2;
    @FXML private Hyperlink backToLoginLink3;
    @FXML private Hyperlink resendLink;
    @FXML private HBox titleBar;
    @FXML private Button btnFullscreen;
    @FXML private Button btnMinimize;
    @FXML private Button btnClose;

    private TextField[] digits;
    private String generatedCode;
    private User userToReset;

    @FXML
    public void initialize() {
        bindWindowChrome();
        Font.loadFont(getClass().getResourceAsStream("/fonts/Rajdhani-Bold.ttf"), 14);
        PrimaryButtonEffects.installGradientPrimary(sendCodeButton);
        PrimaryButtonEffects.installGradientPrimary(verifyCodeButton);
        PrimaryButtonEffects.installGradientPrimary(resetPasswordButton);

        scrollContent.minHeightProperty().bind(scrollPane.heightProperty());

        Platform.runLater(() -> {
            Scene scene = scrollPane.getScene();
            if (scene != null) {
                MainApp.bindRootToScene(scene);
            }
        });

        digits = new TextField[]{digit1, digit2, digit3, digit4, digit5, digit6};
        UnaryOperator<TextFormatter.Change> filter = ch -> {
            String t = ch.getControlNewText();
            if (t.isEmpty()) {
                return ch;
            }
            if (t.length() == 1 && Character.isDigit(t.charAt(0))) {
                return ch;
            }
            return null;
        };
        for (int i = 0; i < digits.length; i++) {
            int idx = i;
            digits[i].setTextFormatter(new TextFormatter<>(filter));
            digits[i].textProperty().addListener((obs, o, n) -> {
                if (n == null) {
                    return;
                }
                if (n.length() > 1) {
                    digits[idx].setText(n.substring(n.length() - 1));
                    return;
                }
                if (n.length() == 1 && idx < digits.length - 1) {
                    digits[idx + 1].requestFocus();
                }
            });
        }
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailField.getStyleClass().removeAll("field-error");
            emailField.getStyleClass().add("field-error");
            showAlert("Email is required.");
            return;
        }
        if (email.length() < 4) {
            emailField.getStyleClass().removeAll("field-error");
            emailField.getStyleClass().add("field-error");
            showAlert("Email must be at least 4 characters.");
            return;
        }
        if (email.length() > 10000) {
            emailField.getStyleClass().removeAll("field-error");
            emailField.getStyleClass().add("field-error");
            showAlert("Email must be less than 10000 characters.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            emailField.getStyleClass().removeAll("field-error");
            emailField.getStyleClass().add("field-error");
            showAlert("Please enter a valid email address.");
            return;
        }
        emailField.getStyleClass().removeAll("field-error");

        // Find user by email
        UserDAO userDAO = new UserDAO();
        System.out.println("DEBUG: Looking for user with email/username: " + email);
        userToReset = userDAO.findByUsernameOrEmail(email);

        if (userToReset == null) {
            System.out.println("DEBUG: No user found for: " + email);
            showAlert("No account found with this email address.");
            return;
        }

        System.out.println("DEBUG: Found user: " + userToReset.getUsername() + " (" + userToReset.getEmail() + ")");

        // Generate 6-digit code
        generatedCode = String.format("%06d", new Random().nextInt(1000000));

        // Send email in background thread to prevent blocking UI
        sendCodeButton.setDisable(true);
        new Thread(() -> {
            try {
                boolean emailSent = EmailService.sendPasswordResetEmail(
                    userToReset.getEmail(),
                    generatedCode,
                    userToReset.getUsername()
                );

                Platform.runLater(() -> {
                    sendCodeButton.setDisable(false);
                    if (emailSent) {
                        showAlert("Reset code sent! Check your email for the 6-digit verification code.");
                    } else {
                        // Show code in a dialog for easier access
                        Alert codeAlert = new Alert(Alert.AlertType.INFORMATION);
                        codeAlert.setTitle("Password Reset Code");
                        codeAlert.setHeaderText("Your Verification Code");
                        codeAlert.setContentText("Your password reset code is: " + generatedCode);
                        codeAlert.showAndWait();
                    }
                    step2Subtitle.setText("We sent a code to " + email);
                    transitionForward(step1, step2);
                    // Focus on first digit field after transition
                    Platform.runLater(() -> {
                        digit1.requestFocus();
                    });
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    sendCodeButton.setDisable(false);
                    System.err.println("Error sending email: " + e.getMessage());
                    e.printStackTrace();
                    // Show code in a dialog as fallback
                    Alert codeAlert = new Alert(Alert.AlertType.INFORMATION);
                    codeAlert.setTitle("Password Reset Code");
                    codeAlert.setHeaderText("Your Verification Code");
                    codeAlert.setContentText("Your password reset code is: " + generatedCode);
                    codeAlert.showAndWait();
                    step2Subtitle.setText("We sent a code to " + email);
                    transitionForward(step1, step2);
                });
            }
        }).start();
    }

    @FXML
    private void handleVerifyCode() {
        String code = digit1.getText() + digit2.getText() + digit3.getText()
                + digit4.getText() + digit5.getText() + digit6.getText();
        if (code.length() != 6) {
            showAlert("Please enter all 6 digits.");
            return;
        }
        if (!code.equals(generatedCode)) {
            showAlert("Invalid code. Please try again.");
            return;
        }
        transitionForward(step2, step3);
    }

    @FXML
    private void handleResetPassword() {
        String p = newPasswordField.getText();
        String c = confirmNewPasswordField.getText();
        if (p.isEmpty() || c.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }
        if (p.length() < 4) {
            showAlert("Password must be at least 4 characters.");
            return;
        }
        if (p.length() > 10000) {
            showAlert("Password must be less than 10000 characters.");
            return;
        }
        if (!p.equals(c)) {
            showAlert("Passwords do not match.");
            return;
        }
        
        // Update password in database
        try {
            UserDAO userDAO = new UserDAO();
            System.out.println("DEBUG: Updating password for user: " + userToReset.getUsername());
            userToReset.setPassword(p);
            System.out.println("DEBUG: New password set: " + p);
            userDAO.update(userToReset);
            System.out.println("DEBUG: Password updated successfully in database");
            
            step3Form.setVisible(false);
            step3Form.setManaged(false);
            successPane.setVisible(true);
            successPane.setManaged(true);

            successCheck.setScaleX(0);
            successCheck.setScaleY(0);
            successCheck.setOpacity(0);

            ScaleTransition sc = new ScaleTransition(Duration.millis(400), successCheck);
            sc.setFromX(0);
            sc.setFromY(0);
            sc.setToX(1);
            sc.setToY(1);
            sc.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fd = new FadeTransition(Duration.millis(400), successCheck);
            fd.setFromValue(0);
            fd.setToValue(1);

            ParallelTransition reveal = new ParallelTransition(sc, fd);
            PauseTransition wait = new PauseTransition(Duration.millis(1500));
            SequentialTransition seq = new SequentialTransition(reveal, wait);
            seq.setOnFinished(e -> navigateToLogin());
            seq.play();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to reset password. Please try again.");
        }
    }

    private void transitionForward(Region from, Region to) {
        System.out.println("DEBUG: Transitioning from " + from.getId() + " to " + to.getId());
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), from);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            System.out.println("DEBUG: Fade out complete, hiding " + from.getId());
            from.setVisible(false);
            from.setManaged(false);
            
            System.out.println("DEBUG: Showing " + to.getId());
            to.setOpacity(0);
            to.setVisible(true);
            to.setManaged(true);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), to);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setOnFinished(ev -> {
                System.out.println("DEBUG: Fade in complete, " + to.getId() + " is now visible");
            });
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handleResendCode() {
        if (userToReset != null) {
            handleSendCode();
        } else {
            showAlert("Please enter your email first.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneNavigation.replaceSceneContent(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Reset");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
