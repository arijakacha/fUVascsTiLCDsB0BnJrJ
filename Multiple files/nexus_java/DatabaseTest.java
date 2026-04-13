import java.sql.*;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3307/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "";
        
        try {
            System.out.println("Testing database connection...");
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            System.out.println("Password: " + (password.isEmpty() ? "(empty)" : "***"));
            
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful!");
            
            // Test database query
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Product Name: " + meta.getDatabaseProductName());
            System.out.println("Database Product Version: " + meta.getDatabaseProductVersion());
            
            // List tables
            ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println("\nTables in nexusplay database:");
            int tableCount = 0;
            while (tables.next()) {
                System.out.println("  - " + tables.getString("TABLE_NAME"));
                tableCount++;
            }
            System.out.println("Total tables: " + tableCount);
            
            conn.close();
            System.out.println("Connection closed successfully!");
            
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            
            // Try different common passwords
            String[] passwords = {"", "root", "password", "1234", "admin"};
            for (String pwd : passwords) {
                try {
                    System.out.println("\nTrying with password: " + (pwd.isEmpty() ? "(empty)" : pwd));
                    Connection conn = DriverManager.getConnection(url, username, pwd);
                    System.out.println("Success with password: " + (pwd.isEmpty() ? "(empty)" : pwd));
                    conn.close();
                    break;
                } catch (SQLException ex) {
                    System.out.println("Failed with password: " + (pwd.isEmpty() ? "(empty)" : pwd));
                }
            }
        }
    }
}
