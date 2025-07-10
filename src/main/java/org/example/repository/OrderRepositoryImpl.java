package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.enums.OrderStatus;
import org.example.model.Order;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRepositoryImpl implements OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @Override
    public List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters) {
        logger.debug("Finding orders for restaurant ID: {} with filters", restaurantId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Start with the base query
            StringBuilder hql = new StringBuilder("SELECT o FROM Order o JOIN o.customer c WHERE o.restaurant.id = :restaurantId");
            Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("restaurantId", restaurantId);

            // Dynamically add conditions based on filters
            if (filters.containsKey("status") && filters.get("status")[0] != null) {
                hql.append(" AND o.status = :status");
                parameters.put("status", OrderStatus.valueOf(filters.get("status")[0].toUpperCase()));
            }
            if (filters.containsKey("user") && filters.get("user")[0] != null) {
                hql.append(" AND lower(c.firstName || ' ' || c.lastName) LIKE :userName");
                parameters.put("userName", "%" + filters.get("user")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("courier") && filters.get("courier")[0] != null) {
                hql.append(" AND o.courier.id = :courierId");
                parameters.put("courierId", Long.parseLong(filters.get("courier")[0]));
            }
            // A generic search can be added here if needed

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByRestaurantIdWithFilters", e);
            return new ArrayList<>();
        }
    }
}
