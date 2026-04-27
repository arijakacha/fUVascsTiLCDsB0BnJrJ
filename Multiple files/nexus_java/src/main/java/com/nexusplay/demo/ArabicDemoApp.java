package com.nexusplay.demo;

import com.nexusplay.util.ArabicSupport;
import com.nexusplay.ui.LanguageSwitcher;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Demo application to test Arabic language support
 * Shows various UI components with Arabic text and RTL layout
 */
public class ArabicDemoApp extends Application {
    
    private VBox mainContainer;
    private LanguageSwitcher languageSwitcher;
    private Label titleLabel;
    private Label welcomeLabel;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private TextArea messageArea;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Arabic Support Demo - منصة نكسوس بلاي");
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        Scene scene = new Scene(mainContainer, 800, 600);
        
        // Load Arabic CSS
        scene.getStylesheets().add(getClass().getResource("/css/arabic.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initial update
        updateUI();
    }
    
    private void initializeComponents() {
        mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        
        // Language switcher
        languageSwitcher = new LanguageSwitcher();
        languageSwitcher.setLanguageChangeListener(this::onLanguageChanged);
        
        // Title
        titleLabel = new Label();
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Welcome message
        welcomeLabel = new Label();
        welcomeLabel.setFont(Font.font("Arial", 16));
        
        // Form fields
        usernameField = new TextField();
        usernameField.setPromptText("Username / اسم المستخدم");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Password / كلمة المرور");
        
        // Buttons
        loginButton = new Button();
        registerButton = new Button();
        
        // Message area
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefHeight(150);
    }
    
    private void setupLayout() {
        // Create form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 10; -fx-background-radius: 10;");
        
        // Add components to form
        formContainer.getChildren().addAll(
            createLabel("login.username"),
            usernameField,
            createLabel("login.password"),
            passwordField,
            createButtonRow(),
            messageArea
        );
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            languageSwitcher,
            titleLabel,
            welcomeLabel,
            formContainer
        );
        
        // Apply RTL layout if Arabic is enabled
        if (ArabicSupport.isArabicEnabled()) {
            ArabicSupport.applyRTLLayout(mainContainer);
        }
    }
    
    private Label createLabel(String key) {
        Label label = new Label();
        ArabicSupport.setArabicText(label, key);
        return label;
    }
    
    private HBox createButtonRow() {
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);
        
        ArabicSupport.setArabicText(loginButton, "login.login");
        ArabicSupport.setArabicText(registerButton, "login.register");
        
        buttonRow.getChildren().addAll(loginButton, registerButton);
        return buttonRow;
    }
    
    private void setupEventHandlers() {
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (ArabicSupport.isArabicEnabled()) {
                messageArea.setText("محاولة تسجيل الدخول:\n" +
                    "اسم المستخدم: " + username + "\n" +
                    "كلمة المرور: " + (password.isEmpty() ? "فارغة" : "••••••••"));
            } else {
                messageArea.setText("Login attempt:\n" +
                    "Username: " + username + "\n" +
                    "Password: " + (password.isEmpty() ? "empty" : "••••••••"));
            }
        });
        
        registerButton.setOnAction(event -> {
            if (ArabicSupport.isArabicEnabled()) {
                messageArea.setText("تم النقر على زر التسجيل الجديد\n" +
                    "سيتم توجيهك إلى صفحة التسجيل قريباً");
            } else {
                messageArea.setText("Register button clicked\n" +
                    "You will be redirected to registration page soon");
            }
        });
        
        // Test Arabic text detection
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (ArabicSupport.containsArabic(newVal)) {
                usernameField.setStyle(ArabicSupport.getArabicDirectionStyle());
            } else {
                usernameField.setStyle("");
            }
        });
    }
    
    private void onLanguageChanged(String language) {
        updateUI();
        
        // Show notification
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Language Changed / تم تغيير اللغة");
        
        if ("ar".equals(language)) {
            alert.setHeaderText("تم تفعيل اللغة العربية");
            alert.setContentText("تم تغيير واجهة التطبيق إلى اللغة العربية مع دعم RTL");
        } else {
            alert.setHeaderText("Language Changed to English");
            alert.setContentText("Application interface has been changed to English");
        }
        
        alert.showAndWait();
    }
    
    private void updateUI() {
        // Update title
        if (ArabicSupport.isArabicEnabled()) {
            titleLabel.setText("تجربة دعم اللغة العربية");
            welcomeLabel.setText("مرحباً بك في منصة نكسوس بلاي");
            usernameField.setPromptText("اسم المستخدم");
            passwordField.setPromptText("كلمة المرور");
        } else {
            titleLabel.setText("Arabic Language Support Demo");
            welcomeLabel.setText("Welcome to NexusPlay Platform");
            usernameField.setPromptText("Username");
            passwordField.setPromptText("Password");
        }
        
        // Update language switcher
        languageSwitcher.updateButtonTexts();
        
        // Update button texts
        ArabicSupport.setArabicText(loginButton, "login.login");
        ArabicSupport.setArabicText(registerButton, "login.register");
        
        // Apply RTL styling
        if (ArabicSupport.isArabicEnabled()) {
            mainContainer.setStyle("-fx-node-orientation: right-to-left;");
            titleLabel.setStyle("-fx-text-alignment: center; -fx-node-orientation: right-to-left;");
            welcomeLabel.setStyle("-fx-text-alignment: center; -fx-node-orientation: right-to-left;");
        } else {
            mainContainer.setStyle("-fx-node-orientation: left-to-right;");
            titleLabel.setStyle("-fx-text-alignment: center; -fx-node-orientation: left-to-right;");
            welcomeLabel.setStyle("-fx-text-alignment: center; -fx-node-orientation: left-to-right;");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
