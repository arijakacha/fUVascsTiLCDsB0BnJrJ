import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import com.nexusplay.config.DatabaseConnection;

public class QueryExecutor {
    public static void main(String[] args) {
        System.out.println("=== EXÉCUTION DE REQUÊTE SQL ===");
        
        // Test de connexion
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("❌ Impossible de se connecter à la base de données");
            System.out.println("Erreur: " + DatabaseConnection.getLastError());
            return;
        }
        
        try {
            // Afficher les informations de la base de données
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("📊 Base de données connectée:");
            System.out.println("   - Produit: " + metaData.getDatabaseProductName());
            System.out.println("   - Version: " + metaData.getDatabaseProductVersion());
            System.out.println("   - URL: " + metaData.getURL());
            
            // Lister les tables
            System.out.println("\n📋 Tables disponibles:");
            Statement stmt = conn.createStatement();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("   - " + tableName);
                
                // Compter les enregistrements dans chaque table
                ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                if (countRs.next()) {
                    System.out.println("     Enregistrements: " + countRs.getInt(1));
                }
                countRs.close();
            }
            
            // Exécuter une requête de test simple
            System.out.println("\n🔍 Requête de test:");
            ResultSet testRs = stmt.executeQuery("SELECT 1 as test_value, NOW() as current_time");
            if (testRs.next()) {
                System.out.println("   - Test value: " + testRs.getInt("test_value"));
                System.out.println("   - Current time: " + testRs.getTimestamp("current_time"));
            }
            testRs.close();
            
            System.out.println("\n✅ Requête exécutée avec succès!");
            
            // Fermer les ressources
            tables.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'exécution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
