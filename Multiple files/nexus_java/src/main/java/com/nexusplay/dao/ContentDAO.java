package com.nexusplay.dao;

import com.nexusplay.entity.Content;
import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class ContentDAO {
    
    public void save(Content content) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (content.getAuthor() != null && content.getAuthor().getId() != null) {
                User managedUser = session.get(User.class, content.getAuthor().getId());
                content.setAuthor(managedUser);
            }

            session.persist(content);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public Content findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(Content.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Content> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Content> query = session.createQuery("SELECT DISTINCT c FROM Content c LEFT JOIN FETCH c.author", Content.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(Content content) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (content.getAuthor() != null && content.getAuthor().getId() != null) {
                User managedUser = session.get(User.class, content.getAuthor().getId());
                content.setAuthor(managedUser);
            }

            session.merge(content);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void delete(Content content) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(content);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<Content> findByType(String type) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Content> query = session.createQuery("FROM Content WHERE type = :type", Content.class);
            query.setParameter("type", type);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Content> findByAuthorId(Integer authorId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Content> query = session.createQuery("FROM Content WHERE author.id = :authorId", Content.class);
            query.setParameter("authorId", authorId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Content> findAllPaginated(int page, int pageSize) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Content> query = session.createQuery(
                "SELECT DISTINCT c FROM Content c LEFT JOIN FETCH c.author ORDER BY c.createdAt DESC", Content.class);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(c) FROM Content c", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
