package com.nexusplay.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Properties;
import com.nexusplay.config.DatabaseConnection;

public class DatabaseConfig {
    private static SessionFactory sessionFactory;
    
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                
                // Database connection settings
                Properties settings = new Properties();
                settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
                settings.put("hibernate.connection.url", DatabaseConnection.getJdbcUrl());
                settings.put("hibernate.connection.username", DatabaseConnection.getDbUser());
                settings.put("hibernate.connection.password", DatabaseConnection.getDbPassword());
                settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                
                // JDBC connection pool settings
                settings.put("hibernate.c3p0.min_size", 5);
                settings.put("hibernate.c3p0.max_size", 20);
                settings.put("hibernate.c3p0.timeout", 300);
                settings.put("hibernate.c3p0.max_statements", 50);
                
                // SQL dialect and settings
                settings.put("hibernate.show_sql", "true");
                settings.put("hibernate.format_sql", "true");
                settings.put("hibernate.hbm2ddl.auto", "update");
                
                configuration.setProperties(settings);
                
                // Add entity classes
                configuration.addAnnotatedClass(com.nexusplay.entity.User.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Player.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Team.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Game.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Achievement.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Coach.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Statistic.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.CoachingSession.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Content.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Conversation.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.ConversationParticipant.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.ForumPost.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.GameMatch.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.GuideComment.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Like.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.MatchPlayer.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Message.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Organization.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.PlayerAchievement.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Product.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.ProductPurchase.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.RankHistory.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Reponse.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Report.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Stream.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.TeamInvitation.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Notification.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.PasswordResetToken.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.Payment.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.UserOrder.class);
                configuration.addAnnotatedClass(com.nexusplay.entity.VirtualCurrency.class);

                sessionFactory = configuration.buildSessionFactory();
                
            } catch (Exception e) {
                System.err.println("Database connection failed: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize database connection", e);
            }
        }
        return sessionFactory;
    }
    
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
