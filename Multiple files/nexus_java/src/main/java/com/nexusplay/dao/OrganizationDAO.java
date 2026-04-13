package com.nexusplay.dao;

import com.nexusplay.entity.Organization;
import com.nexusplay.config.DatabaseConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class OrganizationDAO {
    
    public void save(Organization organization) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(organization);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public Organization findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(Organization.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Organization> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Organization> query = session.createQuery("FROM Organization", Organization.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(Organization organization) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(organization);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(Organization organization) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(organization);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
