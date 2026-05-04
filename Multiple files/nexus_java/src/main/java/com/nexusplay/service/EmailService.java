package com.nexusplay.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    
    // Email configuration - using Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "akaichiiyed10@gmail.com";
    private static final String SMTP_PASSWORD = "ioxa vpet nwhy bqyg";
    




    
    /**
     * Send password reset email with verification code
     */
    public static boolean sendPasswordResetEmail(String toEmail, String resetCode, String username) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("NexusPlay - Password Reset Code");
            
            String emailBody = createEmailBody(username, resetCode);
            message.setText(emailBody);
            
            Transport.send(message);
            
            System.out.println("✅ Password reset email sent to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to console
            System.out.println("========================================");
            System.out.println("EMAIL FAILED - FALLBACK TO CONSOLE:");
            System.out.println("PASSWORD RESET CODE: " + resetCode);
            System.out.println("Email: " + toEmail);
            System.out.println("========================================");
            return false;
        }
    }
    
    /**
     * Create email body with reset code
     */
    private static String createEmailBody(String username, String resetCode) {
        return "Hello " + username + ",\n\n" +
               "We received a request to reset your password for your NexusPlay account.\n\n" +
               "Your password reset code is: " + resetCode + "\n\n" +
               "Enter this code in the application to reset your password.\n\n" +
               "This code will expire in 10 minutes for security reasons.\n\n" +
               "If you didn't request this password reset, please ignore this email.\n\n" +
               "Thank you,\n" +
               "The NexusPlay Team";
    }
}
