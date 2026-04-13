package com.nexusplay.dao;

import com.nexusplay.entity.GuideComment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class GuideCommentDAO {
    private SessionFactory sessionFactory;

    public GuideCommentDAO() {
        this.sessionFactory = com.nexusplay.config.DatabaseConfig.getSessionFactory();
    }

    public void save(GuideComment comment) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            
            // Re-fetch User within this session to avoid detached entity issue
            if (comment.getUser() != null && comment.getUser().getId() != null) {
                com.nexusplay.entity.User managedUser = session.get(com.nexusplay.entity.User.class, comment.getUser().getId());
                if (managedUser != null) {
                    comment.setUser(managedUser);
                }
            }
            
            // Re-fetch Content (guide) within this session to avoid detached entity issue
            if (comment.getGuide() != null && comment.getGuide().getId() != null) {
                com.nexusplay.entity.Content managedGuide = session.get(com.nexusplay.entity.Content.class, comment.getGuide().getId());
                if (managedGuide != null) {
                    comment.setGuide(managedGuide);
                }
            }
            
            // Re-fetch ForumPost within this session to avoid detached entity issue
            if (comment.getPost() != null && comment.getPost().getId() != null) {
                com.nexusplay.entity.ForumPost managedPost = session.get(com.nexusplay.entity.ForumPost.class, comment.getPost().getId());
                if (managedPost != null) {
                    comment.setPost(managedPost);
                }
            }
            
            session.save(comment);
            transaction.commit();
            System.out.println("GuideCommentDAO: Comment saved successfully");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println("GuideCommentDAO: Error saving comment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void update(GuideComment comment) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.update(comment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void delete(GuideComment comment) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.delete(comment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GuideComment findById(Integer id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.get(GuideComment.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<GuideComment> findAll() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<GuideComment> query = session.createQuery("FROM GuideComment", GuideComment.class);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<GuideComment> findByForumPost(Integer postId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<GuideComment> query = session.createQuery(
                "SELECT DISTINCT gc FROM GuideComment gc LEFT JOIN FETCH gc.user WHERE gc.post.id = :postId ORDER BY gc.createdAt DESC", 
                GuideComment.class);
            query.setParameter("postId", postId);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<GuideComment> findByContent(Integer contentId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<GuideComment> query = session.createQuery(
                "SELECT DISTINCT gc FROM GuideComment gc LEFT JOIN FETCH gc.user WHERE gc.guide.id = :contentId ORDER BY gc.createdAt DESC", GuideComment.class);
            query.setParameter("contentId", contentId);
            return query.list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
