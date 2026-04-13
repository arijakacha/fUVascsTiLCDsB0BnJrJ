package com.nexusplay.dao;

import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.entity.Notification;
import com.nexusplay.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDAO {

    public void save(Notification notification) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(notification);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Notification> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n ORDER BY n.createdAt DESC", Notification.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Notification> findByUser(User user) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC", Notification.class);
            query.setParameter("user", user);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Notification> findUnread() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC", Notification.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Notification> findUnreadByUser(User user) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC", Notification.class);
            query.setParameter("user", user);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void markAsRead(Integer notificationId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Notification notification = session.get(Notification.class, notificationId);
            if (notification != null) {
                notification.setIsRead(true);
                session.merge(notification);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(User user) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<Notification> query = session.createQuery(
                "UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false", Notification.class);
            query.setParameter("user", user);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long countUnread() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.isRead = false", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long countUnreadByUser(User user) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false", Long.class);
            query.setParameter("user", user);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void delete(Notification notification) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(notification);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteOldNotifications(LocalDateTime beforeDate) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<Notification> query = session.createQuery(
                "DELETE FROM Notification n WHERE n.createdAt < :beforeDate", Notification.class);
            query.setParameter("beforeDate", beforeDate);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
