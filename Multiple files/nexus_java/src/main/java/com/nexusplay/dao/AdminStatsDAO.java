package com.nexusplay.dao;

import com.nexusplay.config.DatabaseConfig;
import org.hibernate.Session;

/**
 * Aggregated counts and sums for the admin back office dashboard.
 * Uses HQL for core entities and native SQL for optional tables (shop, content, orders)
 * so missing tables fail gracefully at query time rather than at SessionFactory startup.
 */
public class AdminStatsDAO {

    public long countUsers() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Long n = session.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            return n != null ? n : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public long countPlayers() {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Long n = session.createQuery("SELECT COUNT(p) FROM Player p", Long.class).getSingleResult();
            return n != null ? n : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public long countProducts() {
        return longNative("SELECT COUNT(*) FROM product");
    }

    public long countContent() {
        return longNative("SELECT COUNT(*) FROM content");
    }

    /**
     * Sum of completed order amounts; falls back to product_purchase totals if no orders table.
     */
    public double sumCompletedRevenue() {
        Double fromOrders = doubleNative(
                "SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status = 'COMPLETED'");
        if (fromOrders != null) {
            return fromOrders;
        }
        return doubleNative("SELECT COALESCE(SUM(quantity * unit_price), 0) FROM product_purchase");
    }

    /**
     * Pending orders; uses orders table if present, otherwise counts product_purchase rows.
     */
    public long countPendingOrders() {
        Long n = longNative("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'");
        if (n != null) {
            return n;
        }
        Long fallback = longNative("SELECT COUNT(*) FROM product_purchase");
        return fallback != null ? fallback : 0L;
    }

    private static Long longNative(String sql) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Object r = session.createNativeQuery(sql, Object.class).uniqueResult();
            if (r == null) {
                return null;
            }
            if (r instanceof Number) {
                return ((Number) r).longValue();
            }
            return Long.parseLong(r.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static Double doubleNative(String sql) {
        try (Session session = DatabaseConfig.getSessionFactory().openSession()) {
            Object r = session.createNativeQuery(sql, Object.class).uniqueResult();
            if (r == null) {
                return null;
            }
            if (r instanceof Number) {
                return ((Number) r).doubleValue();
            }
            return Double.parseDouble(r.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
