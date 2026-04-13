package com.nexusplay.dao;

import com.nexusplay.entity.Player;
import com.nexusplay.config.DatabaseConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class PlayerDAO {
    
    public void save(Player player) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public Player findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(Player.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Player> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Player> query = session.createQuery("FROM Player", Player.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(Player player) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void delete(Player player) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<Player> findByGameId(Integer gameId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Player> query = session.createQuery("FROM Player WHERE game.id = :gameId", Player.class);
            query.setParameter("gameId", gameId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
