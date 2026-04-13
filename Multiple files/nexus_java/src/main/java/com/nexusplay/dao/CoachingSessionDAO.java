package com.nexusplay.dao;

import com.nexusplay.entity.CoachingSession;
import com.nexusplay.config.DatabaseConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class CoachingSessionDAO {
    
    public void save(CoachingSession session) {
        Transaction transaction = null;
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = hibernateSession.beginTransaction();
            hibernateSession.persist(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public CoachingSession findById(Integer id) {
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            return hibernateSession.get(CoachingSession.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<CoachingSession> findAll() {
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            Query<CoachingSession> query = hibernateSession.createQuery("FROM CoachingSession", CoachingSession.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<CoachingSession> findByCoachId(Integer coachId) {
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            Query<CoachingSession> query = hibernateSession.createQuery("FROM CoachingSession WHERE coach.id = :coachId", CoachingSession.class);
            query.setParameter("coachId", coachId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<CoachingSession> findByPlayerId(Integer playerId) {
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            Query<CoachingSession> query = hibernateSession.createQuery("FROM CoachingSession WHERE player.id = :playerId", CoachingSession.class);
            query.setParameter("playerId", playerId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(CoachingSession session) {
        Transaction transaction = null;
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = hibernateSession.beginTransaction();
            hibernateSession.merge(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(CoachingSession session) {
        Transaction transaction = null;
        try (Session hibernateSession = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = hibernateSession.beginTransaction();
            hibernateSession.remove(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
