package com.nexusplay.dao;

import com.nexusplay.entity.UserOrder;
import com.nexusplay.config.DatabaseConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class OrderDAO {
    
    public void save(UserOrder order) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public UserOrder findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(UserOrder.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<UserOrder> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<UserOrder> query = session.createQuery("FROM UserOrder", UserOrder.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(UserOrder order) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(UserOrder order) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<UserOrder> findByUserId(Integer userId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<UserOrder> query = session.createQuery("FROM UserOrder WHERE user.id = :userId", UserOrder.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<UserOrder> findByStatus(String status) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<UserOrder> query = session.createQuery("FROM UserOrder WHERE status = :status", UserOrder.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
