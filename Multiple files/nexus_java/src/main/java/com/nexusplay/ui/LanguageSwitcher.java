package com.nexusplay.ui;

import com.nexusplay.util.ArabicSupport;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Language switcher component for Arabic/English support
 * Provides toggle buttons to switch between Arabic and English languages
 */
public class LanguageSwitcher extends HBox {
    
    private Button englishButton;
    private Button arabicButton;
    private LanguageChangeListener listener;
    
    public interface LanguageChangeListener {
        void onLanguageChanged(String language);
    }
    
    public LanguageSwitcher() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateButtonStates();
    }
    
    private void initializeComponents() {
        // English button
        englishButton = new Button("English");
        englishButton.setTooltip(new Tooltip("Switch to English"));
        englishButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Arabic button
        arabicButton = new Button("العربية");
        arabicButton.setTooltip(new Tooltip("التبديل إلى العربية"));
        arabicButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-border-radius: 5; -fx-background-radius: 5;");
    }
    
    private void setupLayout() {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));
        
        // Add spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        getChildren().addAll(spacer, englishButton, arabicButton);
    }
    
    private void setupEventHandlers() {
        englishButton.setOnAction(event -> {
            switchToEnglish();
        });
        
        arabicButton.setOnAction(event -> {
            switchToArabic();
        });
    }
    
    private void switchToEnglish() {
        ArabicSupport.disableArabic();
        updateButtonStates();
        notifyLanguageChanged("en");
    }
    
    private void switchToArabic() {
        ArabicSupport.enableArabic();
        updateButtonStates();
        notifyLanguageChanged("ar");
    }
    
    private void updateButtonStates() {
        boolean isArabic = ArabicSupport.isArabicEnabled();
        
        if (isArabic) {
            // Arabic is active
            arabicButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold;");
            englishButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            // English is active
            englishButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold;");
            arabicButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
    }
    
    private void notifyLanguageChanged(String language) {
        if (listener != null) {
            listener.onLanguageChanged(language);
        }
    }
    
    public void setLanguageChangeListener(LanguageChangeListener listener) {
        this.listener = listener;
    }
    
    public void updateButtonTexts() {
        if (ArabicSupport.isArabicEnabled()) {
            englishButton.setText("English");
            arabicButton.setText("العربية");
            englishButton.setTooltip(new Tooltip("Switch to English"));
            arabicButton.setTooltip(new Tooltip("التبديل إلى العربية"));
        } else {
            englishButton.setText("English");
            arabicButton.setText("Arabic");
            englishButton.setTooltip(new Tooltip("Switch to English"));
            arabicButton.setTooltip(new Tooltip("Switch to Arabic"));
        }
    }
    
    /**
     * Get current language code
     */
    public String getCurrentLanguage() {
        return ArabicSupport.isArabicEnabled() ? "ar" : "en";
    }
    
    /**
     * Set language programmatically
     */
    public void setLanguage(String languageCode) {
        if ("ar".equals(languageCode)) {
            switchToArabic();
        } else {
            switchToEnglish();
        }
    }
}
