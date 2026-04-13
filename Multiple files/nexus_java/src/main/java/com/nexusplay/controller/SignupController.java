package com.nexusplay.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class SignupController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Button signupButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private CheckBox termsCheckBox;
    
    private UserDAO userDAO;
    
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        
        // Initialize user type ComboBox
        userTypeComboBox.getItems().addAll("Player", "Coach", "Organization", "Visitor");
        userTypeComboBox.setValue("Player"); // Default selection
        
        // Add enter key support
        confirmPasswordField.setOnAction(e -> handleSignup());
    }
    
    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String userTypeStr = userTypeComboBox.getValue();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }

        if (username.length() < 4) {
            showError("Username must be at least 4 characters long");
            return;
        }

        if (username.length() > 10000) {
            showError("Username must be less than 10000 characters");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Please enter a valid email address");
            return;
        }

        if (email.length() > 10000) {
            showError("Email must be less than 10000 characters");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters long");
            return;
        }

        if (password.length() > 10000) {
            showError("Password must be less than 10000 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (!termsCheckBox.isSelected()) {
            showError("Please accept the terms and conditions");
            return;
        }
        
        try {
            // Check if username already exists
            if (userDAO.findByUsername(username) != null) {
                showError("Username already exists. Please choose another one.");
                return;
            }
            
            // Check if email already exists
            List<User> existingUsers = userDAO.findAll();
            for (User user : existingUsers) {
                if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                    showError("Email already registered. Please use another email.");
                    return;
                }
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password); // In real app, hash this password
            newUser.setStatus(User.UserStatus.ACTIVE);
            newUser.setCreatedAt(LocalDateTime.now());
            
            // Set user type
            switch (userTypeStr.toLowerCase()) {
                case "admin":
                    newUser.setUserType(User.UserType.ADMIN);
                    break;
                case "coach":
                    newUser.setUserType(User.UserType.COACH);
                    break;
                case "organization":
                    newUser.setUserType(User.UserType.ORGANIZATION);
                    break;
                case "visitor":
                    newUser.setUserType(User.UserType.VISITOR);
                    break;
                case "player":
                default:
                    newUser.setUserType(User.UserType.REGISTERED);
                    break;
            }
            
            // Save user to database
            userDAO.save(newUser);
            
            // Debug: print all users to verify
            System.out.println("\n=== DEBUG: All users in database after save ===");
            try {
                java.util.List<com.nexusplay.entity.User> allUsers = userDAO.findAll();
                System.out.println("Total users in DB: " + allUsers.size());
                for (com.nexusplay.entity.User u : allUsers) {
                    System.out.printf("ID=%d, username=%s, email=%s, created=%s\n",
                            u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt());
                }
                System.out.println("=== END DEBUG ===\n");
            } catch (Exception e) {
                System.out.println("Error fetching users: " + e.getMessage());
            }
            
            // Show success message and navigate to login
            showAlert("Account created successfully! Please login with your credentials.");
            navigateToLogin();
            
        } catch (Exception e) {
            showError("Failed to create account: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBack() {
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("NexusPlay - Login");
            stage.setScene(new Scene(root, 1000, 700));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
            
            // Close signup window
            Stage signupStage = (Stage) signupButton.getScene().getWindow();
            signupStage.close();
            
        } catch (Exception e) {
            showError("Failed to return to login: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Hide error after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleMinimizeWindow(MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void handleCloseWindow(MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
