package com.nexusplay.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import java.util.MissingResourceException;

/**
 * Utility class for Arabic language support in NexusPlay Gaming Platform
 * Provides methods for RTL layout, Arabic text loading, and localization
 */
public class ArabicSupport {
    
    private static final String BUNDLE_NAME = "messages";
    private static ResourceBundle arabicBundle;
    private static boolean isArabicLocale = false;
    
    static {
        try {
            arabicBundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ar"));
        } catch (Exception e) {
            System.out.println("Arabic resource bundle not found, using default");
            arabicBundle = null;
        }
    }
    
    /**
     * Enable Arabic locale for the application
     */
    public static void enableArabic() {
        Locale.setDefault(new Locale("ar"));
        isArabicLocale = true;
        System.setProperty("javafx.scene.text.font", "Arial");
        System.out.println("✅ Arabic locale enabled");
    }
    
    /**
     * Disable Arabic locale (switch to default)
     */
    public static void disableArabic() {
        Locale.setDefault(Locale.getDefault());
        isArabicLocale = false;
        System.out.println("✅ Arabic locale disabled");
    }
    
    /**
     * Check if Arabic locale is currently active
     */
    public static boolean isArabicEnabled() {
        return isArabicLocale;
    }
    
    /**
     * Get Arabic text for a given key
     */
    public static String getArabicText(String key) {
        if (!isArabicLocale || arabicBundle == null) {
            return key; // Return key if Arabic is not enabled or bundle not found
        }
        
        try {
            return arabicBundle.getString(key);
        } catch (MissingResourceException e) {
            System.out.println("⚠️ Arabic text not found for key: " + key);
            return key; // Return key if translation not found
        }
    }
    
    /**
     * Get formatted Arabic text with parameters
     */
    public static String getArabicText(String key, Object... params) {
        String text = getArabicText(key);
        return MessageFormat.format(text, params);
    }
    
    /**
     * Apply Arabic text to a label
     */
    public static void setArabicText(Label label, String key) {
        if (label != null) {
            String text = getArabicText(key);
            label.setText(text);
            
            if (isArabicLocale) {
                // Apply RTL styling for Arabic
                label.setStyle("-fx-text-alignment: right; -fx-alignment: center-right;");
            }
        }
    }
    
    /**
     * Apply Arabic text to a button
     */
    public static void setArabicText(Button button, String key) {
        if (button != null) {
            String text = getArabicText(key);
            button.setText(text);
        }
    }
    
    /**
     * Apply Arabic text to a menu item
     */
    public static void setArabicText(MenuItem menuItem, String key) {
        if (menuItem != null) {
            String text = getArabicText(key);
            menuItem.setText(text);
        }
    }
    
    /**
     * Check if a string contains Arabic characters
     */
    public static boolean containsArabic(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) || // Arabic block
                (c >= 0x0750 && c <= 0x077F) || // Arabic Supplement
                (c >= 0x08A0 && c <= 0x08FF)) {  // Arabic Extended-A
                return true;
            }
        }
        return false;
    }
    
    /**
     * Reverse text for proper Arabic display in some contexts
     */
    public static String reverseForArabic(String text) {
        if (!containsArabic(text)) {
            return text;
        }
        return new StringBuilder(text).reverse().toString();
    }
    
    /**
     * Get CSS style for Arabic text direction
     */
    public static String getArabicDirectionStyle() {
        return isArabicLocale ? 
            "-fx-text-alignment: right; -fx-alignment: center-right; -fx-node-orientation: right-to-left;" : 
            "";
    }
    
    /**
     * Apply RTL layout to a node
     */
    public static void applyRTLLayout(javafx.scene.Node node) {
        if (isArabicLocale && node != null) {
            node.setStyle(node.getStyle() + " -fx-node-orientation: right-to-left;");
        }
    }
}
