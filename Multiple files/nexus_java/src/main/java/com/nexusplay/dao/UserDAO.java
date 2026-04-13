package com.nexusplay.dao;

import com.nexusplay.entity.User;
import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.config.DatabaseConnection;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public void save(User user) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = DatabaseConfig.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            try {
                session.doWork(conn -> {
                    try {
                        String url = conn.getMetaData().getURL();
                        String dbUser = conn.getMetaData().getUserName();
                        String dbName;
                        try (java.sql.Statement st = conn.createStatement();
                             java.sql.ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                            dbName = rs.next() ? rs.getString(1) : null;
                        }
                        System.out.println("✅ Hibernate JDBC connected. url=" + url + ", user=" + dbUser + ", database=" + dbName);
                    } catch (Exception ex) {
                        System.out.println("⚠️ Could not read JDBC metadata: " + ex.getMessage());
                    }
                });
            } catch (Exception ignored) {
            }
            session.persist(user);
            session.flush();
            System.out.println("✅ User persisted. Generated ID=" + user.getId() + ", username=" + user.getUsername() + ", email=" + user.getEmail());
            transaction.commit();
            System.out.println("✅ Transaction committed for user ID=" + user.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public User findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return users;
        }

        try (conn) {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    
    public List<User> findAllHibernate() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public User findByUsername(String username) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resolves login by username or email (users often type their email in the first field).
     */
    public User findByUsernameOrEmail(String login) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE u.username = :login OR u.email = :login", User.class);
            query.setParameter("login", login);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Login via JDBC: table {@code user} (backticks — MySQL reserved word).
     * Plain password match only (matches current DB data).
     */
    public static User login(String username, String password) {
        String sql = "SELECT * FROM `user` WHERE (username = ? OR email = ?) AND password = ?";

        DatabaseConnection.clearLastError();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return null;
        }

        try (conn) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, username);
                stmt.setString(3, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = mapRowToUser(rs);
                        System.out.println("✅ Login success: " + user.getUsername()
                                + " | type: " + user.getUserType());
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ SQL error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("❌ No match for: " + username);
        return null;
    }

    private static User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        try {
            user.setProfilePicture(rs.getString("profile_picture"));
        } catch (SQLException ignored) {
            user.setProfilePicture(null);
        }
        String ut = rs.getString("user_type");
        if (ut != null && !ut.isBlank()) {
            user.setUserType(User.UserType.valueOf(ut.trim().toUpperCase()));
        }
        String st = rs.getString("status");
        if (st != null && !st.isBlank()) {
            user.setStatus(User.UserStatus.valueOf(st.trim().toUpperCase()));
        }
        try {
            user.setHasPlayer(rs.getBoolean("has_player"));
        } catch (SQLException ignored) {
            user.setHasPlayer(false);
        }
        return user;
    }

    public static List<User> getUsersWithPhotos() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM `user` WHERE profile_picture IS NOT NULL AND profile_picture <> ''";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return users;
        }

        try (conn) {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
