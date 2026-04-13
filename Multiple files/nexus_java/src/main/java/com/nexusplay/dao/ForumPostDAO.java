package com.nexusplay.dao;

import com.nexusplay.entity.ForumPost;
import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.config.DatabaseConnection;
import com.nexusplay.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class ForumPostDAO {
    
    public ForumPostDAO() {
        ensureTableExists();
    }
    
    private void ensureTableExists() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("ForumPostDAO: Could not get database connection to check/create table");
            return;
        }
        
        try {
            Statement stmt = conn.createStatement();
            
            // Check if table exists
            var rs = stmt.executeQuery("SHOW TABLES LIKE 'forum_post'");
            if (!rs.next()) {
                System.out.println("ForumPostDAO: Creating forum_post table...");
                
                String createTableSQL = "CREATE TABLE forum_post (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "image VARCHAR(500), " +
                    "content TEXT NOT NULL, " +
                    "created_at DATETIME NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                    ")";
                
                stmt.execute(createTableSQL);
                System.out.println("ForumPostDAO: forum_post table created successfully");
            } else {
                System.out.println("ForumPostDAO: forum_post table already exists");
            }
            
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("ForumPostDAO: Error checking/creating table: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    public void save(ForumPost post) {
        Transaction transaction = null;
        Session session = null;
        try {
            System.out.println("ForumPostDAO.save: Opening session...");
            session = DatabaseConfig.getSessionFactory().openSession();
            System.out.println("ForumPostDAO.save: Session opened successfully");
            
            transaction = session.beginTransaction();
            System.out.println("ForumPostDAO.save: Transaction started");

            if (post.getCreatedAt() == null) {
                post.setCreatedAt(LocalDateTime.now());
                System.out.println("ForumPostDAO.save: Set createdAt to " + post.getCreatedAt());
            }

            if (post.getUser() != null && post.getUser().getId() != null) {
                System.out.println("ForumPostDAO.save: Loading User with id=" + post.getUser().getId());
                User managedUser = session.get(User.class, post.getUser().getId());
                if (managedUser != null) {
                    post.setUser(managedUser);
                    System.out.println("ForumPostDAO.save: User loaded and set successfully");
                } else {
                    System.out.println("ForumPostDAO.save: WARNING - User not found with id=" + post.getUser().getId());
                }
            }

            System.out.println("ForumPostDAO.save: Persisting ForumPost with title=" + post.getTitle());
            session.persist(post);
            System.out.println("ForumPostDAO.save: ForumPost persisted successfully");
            
            transaction.commit();
            System.out.println("ForumPostDAO.save: Transaction committed successfully");
        } catch (Exception e) {
            System.out.println("ForumPostDAO.save: ERROR occurred - " + e.getClass().getName() + ": " + e.getMessage());
            if (transaction != null) {
                System.out.println("ForumPostDAO.save: Rolling back transaction");
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (session != null && session.isOpen()) {
                System.out.println("ForumPostDAO.save: Closing session");
                session.close();
            }
        }
    }
    
    public ForumPost findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(ForumPost.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<ForumPost> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<ForumPost> query = session.createQuery(
                "SELECT DISTINCT fp FROM ForumPost fp LEFT JOIN FETCH fp.user ORDER BY fp.createdAt DESC", 
                ForumPost.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<ForumPost> findAllPaginated(int page, int pageSize) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<ForumPost> query = session.createQuery(
                "SELECT DISTINCT fp FROM ForumPost fp LEFT JOIN FETCH fp.user ORDER BY fp.createdAt DESC", 
                ForumPost.class);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(fp) FROM ForumPost fp", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<ForumPost> findByUserId(Integer userId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<ForumPost> query = session.createQuery("FROM ForumPost WHERE user.id = :userId ORDER BY createdAt DESC", ForumPost.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(ForumPost post) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (post.getUser() != null && post.getUser().getId() != null) {
                User managedUser = session.get(User.class, post.getUser().getId());
                post.setUser(managedUser);
            }

            session.merge(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void delete(ForumPost post) {
        if (post == null || post.getId() == null) {
            return;
        }
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("ForumPostDAO.delete: Could not get database connection");
            return;
        }
        
        try {
            // Manually delete related records first to avoid foreign key constraints
            Statement stmt = conn.createStatement();
            
            System.out.println("ForumPostDAO.delete: Deleting related records for post_id=" + post.getId());
            
            // Delete guide_comments
            stmt.executeUpdate("DELETE FROM guide_comment WHERE post_id = " + post.getId());
            System.out.println("ForumPostDAO.delete: Deleted guide_comments");
            
            // Delete likes
            stmt.executeUpdate("DELETE FROM `like` WHERE post_id = " + post.getId());
            System.out.println("ForumPostDAO.delete: Deleted likes");
            
            // Delete reponses
            stmt.executeUpdate("DELETE FROM reponse WHERE post_id = " + post.getId());
            System.out.println("ForumPostDAO.delete: Deleted reponses");
            
            // Delete reports
            stmt.executeUpdate("DELETE FROM report WHERE post_id = " + post.getId());
            System.out.println("ForumPostDAO.delete: Deleted reports");
            
            stmt.close();
            conn.close();
            
            // Now delete the forum post using Hibernate
            Transaction transaction = null;
            try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                ForumPost managed = session.get(ForumPost.class, post.getId());
                if (managed != null) {
                    session.remove(managed);
                    System.out.println("ForumPostDAO.delete: Deleted forum post with id=" + post.getId());
                }
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.out.println("ForumPostDAO.delete: Error - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
