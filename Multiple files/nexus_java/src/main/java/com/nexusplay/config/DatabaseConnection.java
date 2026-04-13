package com.nexusplay.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC for raw SQL (e.g. {@link com.nexusplay.dao.UserDAO#login}).
 * MySQL on port 3307, user root, empty password.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/nexusplayyy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static volatile String lastError;

    private DatabaseConnection() {}

    public static String getJdbcUrl() {
        String value = firstNonBlank(
                System.getProperty("NEXUS_DB_URL"),
                System.getenv("NEXUS_DB_URL"),
                DEFAULT_URL
        );
        return value;
    }

    public static String getDbUser() {
        return firstNonBlank(
                System.getProperty("NEXUS_DB_USER"),
                System.getenv("NEXUS_DB_USER"),
                DEFAULT_USER
        );
    }

    public static String getDbPassword() {
        String value = firstNonBlank(
                System.getProperty("NEXUS_DB_PASSWORD"),
                System.getenv("NEXUS_DB_PASSWORD"),
                DEFAULT_PASSWORD
        );
        return value;
    }





    public static String getLastError() {
        return lastError;
    }

    public static void clearLastError() {
        lastError = null;
    }

    /**
     * @return live connection, or {@code null} on failure
     */
    public static Connection getConnection() {
        String url = getJdbcUrl();
        String user = getDbUser();
        String password = getDbPassword();
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            clearLastError();
            System.out.println("✅ DB Connected: " + url + " as " + user);
            return conn;
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.out.println("❌ DB Connection FAILED: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
