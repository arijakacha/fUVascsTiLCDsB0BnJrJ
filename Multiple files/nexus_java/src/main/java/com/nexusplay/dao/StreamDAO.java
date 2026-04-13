package com.nexusplay.dao;

import com.nexusplay.entity.Stream;
import com.nexusplay.config.DatabaseConfig;
import com.nexusplay.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class StreamDAO {
    
    public void save(Stream stream) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (stream.getPlayer() != null && stream.getPlayer().getId() != null) {
                Player managedPlayer = session.get(Player.class, stream.getPlayer().getId());
                stream.setPlayer(managedPlayer);
            }

            session.persist(stream);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public Stream findById(Integer id) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            return session.get(Stream.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Stream> findAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Stream> query = session.createQuery("SELECT DISTINCT s FROM Stream s LEFT JOIN FETCH s.player", Stream.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long countAll() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(s) FROM Stream s", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<Stream> findAllPaginated(int page, int pageSize) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Stream> query = session.createQuery("SELECT DISTINCT s FROM Stream s LEFT JOIN FETCH s.player", Stream.class);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void update(Stream stream) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (stream.getPlayer() != null && stream.getPlayer().getId() != null) {
                Player managedPlayer = session.get(Player.class, stream.getPlayer().getId());
                stream.setPlayer(managedPlayer);
            }

            session.merge(stream);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void delete(Stream stream) {
        Transaction transaction = null;
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(stream);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<Stream> findByPlayerId(Integer playerId) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Stream> query = session.createQuery("FROM Stream WHERE player.id = :playerId", Stream.class);
            query.setParameter("playerId", playerId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Stream> findLiveStreams() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Query<Stream> query = session.createQuery("FROM Stream WHERE isLive = true", Stream.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
