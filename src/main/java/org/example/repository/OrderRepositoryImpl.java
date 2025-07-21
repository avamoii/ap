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
import java.util.Map;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @Override
    public List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters) {
        logger.debug("Finding orders for restaurant ID: {} with filters", restaurantId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Start with the base query
            // We join with customer 'c' to be able to filter by their name
            StringBuilder hql = new StringBuilder("SELECT o FROM Order o JOIN o.customer c WHERE o.restaurant.id = :restaurantId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("restaurantId", restaurantId);

            // Dynamically add conditions based on filters
            if (filters.containsKey("status") && filters.get("status")[0] != null && !filters.get("status")[0].isEmpty()) {
                hql.append(" AND o.status = :status");
                try {
                    parameters.put("status", OrderStatus.valueOf(filters.get("status")[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status value provided in filter: {}", filters.get("status")[0]);
                    // Optionally, handle invalid status by returning an empty list or ignoring the filter
                    return new ArrayList<>();
                }
            }
            if (filters.containsKey("user") && filters.get("user")[0] != null && !filters.get("user")[0].isEmpty()) {
                // Assuming 'user' filter is by customer's full name (for simplicity)
                hql.append(" AND lower(c.firstName || ' ' || c.lastName) LIKE :userName");
                parameters.put("userName", "%" + filters.get("user")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("courier") && filters.get("courier")[0] != null && !filters.get("courier")[0].isEmpty()) {
                hql.append(" AND o.courier.id = :courierId");
                parameters.put("courierId", Long.parseLong(filters.get("courier")[0]));
            }
            // A generic search can be added here if needed for the 'search' parameter

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            return query.list();
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
                // Initialize all required associations to prevent LazyInitializationException
                Hibernate.initialize(order.getCustomer());
                Hibernate.initialize(order.getRestaurant());
                Hibernate.initialize(order.getRestaurant().getOwner());
                Hibernate.initialize(order.getItems());
                if (order.getCourier() != null) {
                    Hibernate.initialize(order.getCourier());
                }
            }
            return Optional.ofNullable(order);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for order ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Order update(Order order) {
        logger.debug("Attempting to update order with ID: {}", order.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order updatedOrder = session.merge(order);
            transaction.commit();
            logger.info("SUCCESS: Order with ID {} updated.", updatedOrder.getId());
            return updatedOrder;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for order ID {}", order.getId(), e);
            throw new RuntimeException("Could not update order", e);
        }
    }
    @Override
    public Order save(Order order) {
        logger.debug("Attempting to save order for customer ID: {}", order.getCustomer().getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(order);
            session.flush();
            transaction.commit();
            logger.info("SUCCESS: Order saved with ID: {}", order.getId());
            return order;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for order", e);
            throw new RuntimeException("Could not save order", e);
        }
    }
    @Override
    public List<Order> findByCustomerIdWithFilters(Long customerId, Map<String, String[]> filters) {
        logger.debug("Finding order history for customer ID: {} with filters", customerId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Start with the base query and necessary joins
            StringBuilder hql = new StringBuilder("SELECT o FROM Order o JOIN o.restaurant r WHERE o.customer.id = :customerId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("customerId", customerId);

            // Dynamically add conditions based on filters
            if (filters.containsKey("vendor") && filters.get("vendor")[0] != null && !filters.get("vendor")[0].isEmpty()) {
                hql.append(" AND lower(r.name) LIKE :vendorName");
                parameters.put("vendorName", "%" + filters.get("vendor")[0].toLowerCase() + "%");
            }

            // The 'search' can look into restaurant name or item names. This requires another join.
            if (filters.containsKey("search") && filters.get("search")[0] != null && !filters.get("search")[0].isEmpty()) {
                hql.append(" AND (lower(r.name) LIKE :search OR EXISTS (SELECT 1 FROM o.items i WHERE lower(i.name) LIKE :search))");
                parameters.put("search", "%" + filters.get("search")[0].toLowerCase() + "%");
            }

            hql.append(" ORDER BY o.createdAt DESC"); // Show the most recent orders first

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByCustomerIdWithFilters", e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        logger.debug("Finding all orders with status: {}", status);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE status = :status", Order.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByStatus for status {}", status, e);
            return new ArrayList<>();
        }
    }
}

