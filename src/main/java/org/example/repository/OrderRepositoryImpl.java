package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.enums.OrderStatus;
import org.example.model.Order;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @Override
    public List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters) {
        logger.debug("Finding orders for restaurant ID: {} with filters", restaurantId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // --- تغییر: افزودن LEFT JOIN FETCH o.rating ---
            StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM Order o JOIN o.customer c LEFT JOIN FETCH o.rating WHERE o.restaurant.id = :restaurantId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("restaurantId", restaurantId);

            if (filters.containsKey("status") && filters.get("status")[0] != null && !filters.get("status")[0].isEmpty()) {
                hql.append(" AND o.status = :status");
                try {
                    parameters.put("status", OrderStatus.valueOf(filters.get("status")[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status value provided in filter: {}", filters.get("status")[0]);
                    return new ArrayList<>();
                }
            }
            if (filters.containsKey("user") && filters.get("user")[0] != null && !filters.get("user")[0].isEmpty()) {
                hql.append(" AND lower(c.firstName || ' ' || c.lastName) LIKE :userName");
                parameters.put("userName", "%" + filters.get("user")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("courier") && filters.get("courier")[0] != null && !filters.get("courier")[0].isEmpty()) {
                hql.append(" AND o.courier.id = :courierId");
                parameters.put("courierId", Long.parseLong(filters.get("courier")[0]));
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            List<Order> orders = query.list();
            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
            }
            return orders;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByRestaurantIdWithFilters", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        logger.debug("Attempting to find order by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Order order = session.get(Order.class, id);
            if (order != null) {
                Hibernate.initialize(order.getCustomer());
                Hibernate.initialize(order.getRestaurant());
                Hibernate.initialize(order.getRestaurant().getOwner());
                Hibernate.initialize(order.getItems());
                if (order.getCourier() != null) {
                    Hibernate.initialize(order.getCourier());
                }
                // اطمینان از واکشی نظر (برای این متد ضروری است)
                if (order.getRating() != null) {
                    Hibernate.initialize(order.getRating());
                }
            }
            return Optional.ofNullable(order);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for order ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Order save(Order order) {
        logger.debug("Attempting to save order for customer ID: {}", order.getCustomer().getId());
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Order mergedOrder = session.merge(order);
            transaction.commit();
            logger.info("SUCCESS: Order saved with ID: {}", mergedOrder.getId());
            return mergedOrder;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in save method for order", e);
            throw new RuntimeException("Could not save order", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Order update(Order order) {
        logger.debug("Attempting to update order with ID: {}", order.getId());
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Order updatedOrder = session.merge(order);
            transaction.commit();
            logger.info("SUCCESS: Order with ID {} updated.", updatedOrder.getId());
            return updatedOrder;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in update method for order ID {}", order.getId(), e);
            throw new RuntimeException("Could not update order", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Order> findByCustomerIdWithFilters(Long customerId, Map<String, String[]> filters) {
        logger.debug("Finding order history for customer ID: {} with filters", customerId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // --- تغییر: افزودن LEFT JOIN FETCH o.rating ---
            StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM Order o JOIN o.restaurant r LEFT JOIN FETCH o.rating WHERE o.customer.id = :customerId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("customerId", customerId);

            if (filters.containsKey("vendor") && filters.get("vendor")[0] != null && !filters.get("vendor")[0].isEmpty()) {
                hql.append(" AND lower(r.name) LIKE :vendorName");
                parameters.put("vendorName", "%" + filters.get("vendor")[0].toLowerCase() + "%");
            }

            if (filters.containsKey("search") && filters.get("search")[0] != null && !filters.get("search")[0].isEmpty()) {
                hql.append(" AND (lower(r.name) LIKE :search OR EXISTS (SELECT 1 FROM o.items i WHERE lower(i.name) LIKE :search))");
                parameters.put("search", "%" + filters.get("search")[0].toLowerCase() + "%");
            }

            hql.append(" ORDER BY o.createdAt DESC");

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            // با وجود FETCH، دیگر نیازی به initialize دستی نیست، اما برای اطمینان نگه می‌داریم
            List<Order> orders = query.list();
            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
                Hibernate.initialize(order.getCustomer());
                Hibernate.initialize(order.getRestaurant());
                if (order.getCourier() != null) {
                    Hibernate.initialize(order.getCourier());
                }
                if (order.getCoupon() != null) {
                    Hibernate.initialize(order.getCoupon());
                }
            }
            return orders;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByCustomerIdWithFilters", e);
            throw new RuntimeException("Could not fetch order history", e);
        }
    }
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        logger.debug("Finding all orders with status: {}", status);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // --- تغییر: افزودن LEFT JOIN FETCH o.rating ---
            Query<Order> query = session.createQuery("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.rating WHERE o.status = :status", Order.class);
            query.setParameter("status", status);
            List<Order> orders = query.list();

            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
            }

            return orders;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByStatus for status {}", status, e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<Order> findByCourierIdWithFilters(Long courierId, Map<String, String[]> filters) {
        logger.debug("Finding delivery history for courier ID: {} with filters", courierId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // --- تغییر: افزودن LEFT JOIN FETCH o.rating ---
            StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM Order o JOIN o.restaurant r JOIN o.customer c LEFT JOIN FETCH o.rating WHERE o.courier.id = :courierId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("courierId", courierId);

            if (filters.containsKey("vendor") && filters.get("vendor")[0] != null && !filters.get("vendor")[0].isEmpty()) {
                hql.append(" AND lower(r.name) LIKE :vendorName");
                parameters.put("vendorName", "%" + filters.get("vendor")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("user") && filters.get("user")[0] != null && !filters.get("user")[0].isEmpty()) {
                hql.append(" AND lower(c.firstName || ' ' || c.lastName) LIKE :userName");
                parameters.put("userName", "%" + filters.get("user")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("search") && filters.get("search")[0] != null && !filters.get("search")[0].isEmpty()) {
                hql.append(" AND (lower(r.name) LIKE :search OR lower(c.firstName || ' ' || c.lastName) LIKE :search)");
                parameters.put("search", "%" + filters.get("search")[0].toLowerCase() + "%");
            }

            hql.append(" ORDER BY o.createdAt DESC");

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            List<Order> orders = query.list();
            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
            }
            return orders;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByCourierIdWithFilters", e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<Order> findAllWithFilters(Map<String, String[]> filters) {
        logger.debug("Finding all orders with filters: {}", filters);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // --- تغییر: افزودن LEFT JOIN FETCH o.rating ---
            StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM Order o JOIN o.customer c JOIN o.restaurant r LEFT JOIN o.courier co LEFT JOIN FETCH o.rating");
            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            if (filters.containsKey("status") && filters.get("status")[0] != null && !filters.get("status")[0].isEmpty()) {
                try {
                    conditions.add("o.status = :status");
                    parameters.put("status", OrderStatus.valueOf(filters.get("status")[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status value provided in filter: {}", filters.get("status")[0]);
                    return new ArrayList<>();
                }
            }
            if (filters.containsKey("vendor") && filters.get("vendor")[0] != null && !filters.get("vendor")[0].isEmpty()) {
                conditions.add("lower(r.name) LIKE :vendorName");
                parameters.put("vendorName", "%" + filters.get("vendor")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("customer") && filters.get("customer")[0] != null && !filters.get("customer")[0].isEmpty()) {
                conditions.add("lower(c.firstName || ' ' || c.lastName) LIKE :customerName");
                parameters.put("customerName", "%" + filters.get("customer")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("courier") && filters.get("courier")[0] != null && !filters.get("courier")[0].isEmpty()) {
                conditions.add("lower(co.firstName || ' ' || co.lastName) LIKE :courierName");
                parameters.put("courierName", "%" + filters.get("courier")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("search") && filters.get("search")[0] != null && !filters.get("search")[0].isEmpty()) {
                conditions.add("(lower(r.name) LIKE :search OR lower(c.firstName || ' ' || c.lastName) LIKE :search)");
                parameters.put("search", "%" + filters.get("search")[0].toLowerCase() + "%");
            }

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            hql.append(" ORDER BY o.createdAt DESC");

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            List<Order> orders = query.list();
            for (Order order : orders) {
                Hibernate.initialize(order.getItems());
            }
            return orders;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findAllWithFilters", e);
            return new ArrayList<>();
        }
    }
}