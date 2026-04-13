package com.nexusplay.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;

public class GoogleOAuthService {
    
    // Google OAuth 2.0 Configuration
    // Get these from Google Cloud Console: https://console.cloud.google.com/
    private static final String CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID";
    private static final String CLIENT_SECRET = "YOUR_GOOGLE_CLIENT_SECRET";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    
    /**
     * Get Google OAuth authorization URL
     */
    public static String getAuthorizationUrl() {
        return new GoogleBrowserClientRequestUrl(CLIENT_ID, REDIRECT_URI, 
            java.util.Arrays.asList("email", "profile"))
            .build();
    }
    
    /**
     * Exchange authorization code for user info
     */
    public static Userinfo getUserInfo(String authCode) throws IOException {
        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                CLIENT_ID,
                CLIENT_SECRET,
                authCode,
                REDIRECT_URI
            ).execute();
            
            // Simple approach: create Oauth2 with request initializer
            com.google.api.client.http.HttpRequestInitializer initializer = request -> {
                request.getHeaders().setAuthorization("Bearer " + tokenResponse.getAccessToken());
            };
            
            Oauth2 oauth2 = new Oauth2.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                initializer
            ).setApplicationName("NexusPlay").build();
            
            return oauth2.userinfo().get().execute();
            
        } catch (Exception e) {
            System.err.println("Google OAuth error: " + e.getMessage());
            throw new IOException("Failed to get Google user info: " + e.getMessage());
        }
    }
    
    /**
     * Check if Google OAuth is configured
     */
    public static boolean isConfigured() {
        return !CLIENT_ID.equals("YOUR_GOOGLE_CLIENT_ID") && 
               !CLIENT_SECRET.equals("YOUR_GOOGLE_CLIENT_SECRET");
    }
    
    /**
     * Get setup instructions
     */
    public static String getSetupInstructions() {
        return "GOOGLE OAUTH SETUP REQUIRED:\n" +
               "1. Go to Google Cloud Console: https://console.cloud.google.com/\n" +
               "2. Create a new project or select existing one\n" +
               "3. Enable Google+ API\n" +
               "4. Create OAuth 2.0 credentials (Web application)\n" +
               "5. Add http://localhost:8080/callback to authorized redirect URIs\n" +
               "6. Replace CLIENT_ID and CLIENT_SECRET in GoogleOAuthService.java";
    }
}
