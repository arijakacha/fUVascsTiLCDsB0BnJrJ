package com.nexusplay.dao;

import com.nexusplay.entity.Like;
import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.config.DatabaseConnection;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

public class LikeDAO {
    
    public LikeDAO() {
        ensureTableExists();
    }
    
    private void ensureTableExists() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("LikeDAO: Could not get database connection to check/create table");
            return;
        }
        
        try {
            Statement stmt = conn.createStatement();
            
            // Check if table exists
            var rs = stmt.executeQuery("SHOW TABLES LIKE 'post_likes'");
            if (!rs.next()) {
                System.out.println("LikeDAO: Creating post_likes table...");
                
                String createTableSQL = "CREATE TABLE post_likes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "post_id INT, " +
                    "content_id INT, " +
                    "type VARCHAR(10) NOT NULL, " +
                    "created_at DATETIME NOT NULL, " +
                    "UNIQUE KEY unique_user_post (user_id, post_id), " +
                    "UNIQUE KEY unique_user_content (user_id, content_id), " +
                    "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE" +
                    ")";
                
                stmt.execute(createTableSQL);
                System.out.println("LikeDAO: post_likes table created successfully");
            } else {
                System.out.println("LikeDAO: post_likes table already exists, checking schema...");
                
                // Check if content_id column exists
                var columnsRs = stmt.executeQuery("SHOW COLUMNS FROM post_likes LIKE 'content_id'");
                if (!columnsRs.next()) {
                    System.out.println("LikeDAO: Adding content_id column to post_likes table...");
                    stmt.execute("ALTER TABLE post_likes ADD COLUMN content_id INT AFTER post_id");
                    stmt.execute("ALTER TABLE post_likes ADD UNIQUE KEY unique_user_content (user_id, content_id)");
                    stmt.execute("ALTER TABLE post_likes ADD FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE");
                    System.out.println("LikeDAO: content_id column added successfully");
                } else {
                    System.out.println("LikeDAO: content_id column already exists");
                }
                columnsRs.close();

                // Ensure post_id/content_id are nullable (table stores either a post like or a content like)
                try {
                    stmt.execute("ALTER TABLE post_likes MODIFY post_id INT NULL");
                } catch (Exception ignored) {
                }
                try {
                    stmt.execute("ALTER TABLE post_likes MODIFY content_id INT NULL");
                } catch (Exception ignored) {
                }
            }
            
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("LikeDAO: Error checking/creating table: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    public void save(Like like) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Re-fetch User and Post to ensure they're attached to this session
            com.nexusplay.entity.User managedUser = session.get(com.nexusplay.entity.User.class, like.getUser().getId());
            com.nexusplay.entity.ForumPost managedPost = session.get(com.nexusplay.entity.ForumPost.class, like.getPost().getId());
            
            if (managedUser != null && managedPost != null) {
                like.setUser(managedUser);
                like.setPost(managedPost);
                session.persist(like);
                transaction.commit();
            } else {
                throw new RuntimeException("Could not load user or post for like");
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void toggleContentReactionByIdsJdbc(Integer userId, Integer contentId, String type) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new RuntimeException("Could not get database connection");
        }

        try {
            conn.setAutoCommit(false);

            String existingType = null;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT type FROM post_likes WHERE user_id = ? AND content_id = ?"
            )) {
                ps.setInt(1, userId);
                ps.setInt(2, contentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingType = rs.getString(1);
                    }
                }
            }

            if (existingType == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO post_likes (user_id, post_id, content_id, type, created_at) VALUES (?, NULL, ?, ?, ?)"
                )) {
                    ps.setInt(1, userId);
                    ps.setInt(2, contentId);
                    ps.setString(3, type);
                    ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
                    ps.executeUpdate();
                }
            } else if (existingType.equals(type)) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM post_likes WHERE user_id = ? AND content_id = ?"
                )) {
                    ps.setInt(1, userId);
                    ps.setInt(2, contentId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE post_likes SET type = ? WHERE user_id = ? AND content_id = ?"
                )) {
                    ps.setString(1, type);
                    ps.setInt(2, userId);
                    ps.setInt(3, contentId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
            throw new RuntimeException(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void toggleContentReactionByIds(Integer userId, Integer contentId, String type) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            com.nexusplay.entity.User user = session.get(com.nexusplay.entity.User.class, userId);
            com.nexusplay.entity.Content content = session.get(com.nexusplay.entity.Content.class, contentId);

            if (user == null || content == null) {
                throw new RuntimeException("Could not load user or content for like");
            }

            Query<Like> query = session.createQuery(
                "SELECT l FROM Like l WHERE l.user.id = :userId AND l.content.id = :contentId",
                Like.class
            );
            query.setParameter("userId", userId);
            query.setParameter("contentId", contentId);
            Like existingLike = query.uniqueResult();

            if (existingLike == null) {
                Like like = new Like(user, content, type);
                session.persist(like);
            } else if (type != null && type.equals(existingLike.getType())) {
                session.remove(existingLike);
            } else {
                existingLike.setType(type);
                session.merge(existingLike);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void savePostLikeByIds(Integer userId, Integer postId, String type) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Fetch User and Post within this session
            com.nexusplay.entity.User user = session.get(com.nexusplay.entity.User.class, userId);
            com.nexusplay.entity.ForumPost post = session.get(com.nexusplay.entity.ForumPost.class, postId);
            
            if (user != null && post != null) {
                Like like = new Like(user, post, type);
                session.persist(like);
                transaction.commit();
            } else {
                throw new RuntimeException("Could not load user or post for like");
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public Like findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(Like.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Like> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Like> query = session.createQuery("FROM Like", Like.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Like> findByPost(Integer postId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Like> query = session.createQuery("FROM Like WHERE post.id = :postId", Like.class);
            query.setParameter("postId", postId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Like findByUserAndPost(Integer userId, Integer postId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Like> query = session.createQuery(
                "SELECT l FROM Like l LEFT JOIN FETCH l.user LEFT JOIN FETCH l.post WHERE l.user.id = :userId AND l.post.id = :postId", Like.class);
            query.setParameter("userId", userId);
            query.setParameter("postId", postId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Like findByUserAndContent(Integer userId, Integer contentId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Like> query = session.createQuery(
                "SELECT l FROM Like l LEFT JOIN FETCH l.user LEFT JOIN FETCH l.content WHERE l.user.id = :userId AND l.content.id = :contentId", Like.class);
            query.setParameter("userId", userId);
            query.setParameter("contentId", contentId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countLikesByPost(Integer postId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId AND l.type = 'like'", Long.class);
            query.setParameter("postId", postId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public long countDislikesByPost(Integer postId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId AND l.type = 'dislike'", Long.class);
            query.setParameter("postId", postId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public long countLikesByContent(Integer contentId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.content.id = :contentId AND l.type = 'like'", Long.class);
            query.setParameter("contentId", contentId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public long countDislikesByContent(Integer contentId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.content.id = :contentId AND l.type = 'dislike'", Long.class);
            query.setParameter("contentId", contentId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public Like findByUserAndStream(Integer userId, Integer streamId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Like> query = session.createQuery(
                "SELECT l FROM Like l LEFT JOIN FETCH l.user LEFT JOIN FETCH l.stream WHERE l.user.id = :userId AND l.stream.id = :streamId", Like.class);
            query.setParameter("userId", userId);
            query.setParameter("streamId", streamId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countLikesByStream(Integer streamId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.stream.id = :streamId AND l.type = 'like'", Long.class);
            query.setParameter("streamId", streamId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public long countDislikesByStream(Integer streamId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Like l WHERE l.stream.id = :streamId AND l.type = 'dislike'", Long.class);
            query.setParameter("streamId", streamId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public void toggleStreamReactionByIdsJdbc(Integer userId, Integer streamId, String type) {
        java.sql.Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Check if reaction already exists
            String checkSql = "SELECT id, type FROM post_likes WHERE user_id = ? AND stream_id = ?";
            java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, streamId);
            java.sql.ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Existing reaction
                int existingId = rs.getInt("id");
                String existingType = rs.getString("type");
                
                if (type.equals(existingType)) {
                    // Same type - remove reaction
                    String deleteSql = "DELETE FROM post_likes WHERE id = ?";
                    java.sql.PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                    deleteStmt.setInt(1, existingId);
                    deleteStmt.executeUpdate();
                } else {
                    // Different type - update type
                    String updateSql = "UPDATE post_likes SET type = ? WHERE id = ?";
                    java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, type);
                    updateStmt.setInt(2, existingId);
                    updateStmt.executeUpdate();
                }
            } else {
                // New reaction - insert
                String insertSql = "INSERT INTO post_likes (user_id, stream_id, post_id, content_id, type, created_at) VALUES (?, ?, NULL, NULL, ?, NOW())";
                java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, streamId);
                insertStmt.setString(3, type);
                insertStmt.executeUpdate();
            }
            
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void update(Like like) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(like);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(Like like) {
        if (like == null || like.getId() == null) {
            System.out.println("LikeDAO: Cannot delete null like or like with null id");
            return;
        }
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Re-fetch the entity within this session to avoid detached entity issue
            Like managedLike = session.get(Like.class, like.getId());
            if (managedLike != null) {
                session.remove(managedLike);
                transaction.commit();
                System.out.println("LikeDAO: Like deleted successfully, id=" + like.getId());
            } else {
                System.out.println("LikeDAO: Like not found with id=" + like.getId());
                if (transaction != null) transaction.rollback();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println("LikeDAO: Error deleting like: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
