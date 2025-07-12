package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Restaurant;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RestaurantRepositoryImpl implements RestaurantRepository {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantRepositoryImpl.class);

    @Override
    public Restaurant save(Restaurant restaurant) {
        logger.debug("Attempting to save restaurant: {}", restaurant.getName());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(restaurant);
            session.flush();
            transaction.commit();
            logger.info("SUCCESS: Restaurant saved with ID: {}", restaurant.getId());
            return restaurant;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for restaurant {}", restaurant.getName(), e);
            throw new RuntimeException("Could not save restaurant", e);
        }
    }

    @Override
    public List<Restaurant> findByOwnerId(Long ownerId) {
        logger.debug("Attempting to find restaurants for owner ID: {}", ownerId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE owner.id = :ownerId", Restaurant.class);
            query.setParameter("ownerId", ownerId);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByOwnerId for owner ID {}", ownerId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        logger.debug("Attempting to find restaurant by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, id);

            // This is crucial to prevent LazyInitializationException in the Action layer.
            if (restaurant != null) {
                // We explicitly initialize all the associations we know will be needed.
                Hibernate.initialize(restaurant.getOwner());
                Hibernate.initialize(restaurant.getMenus());
                // For each menu, also initialize its list of food items.
                restaurant.getMenus().forEach(menu -> Hibernate.initialize(menu.getFoodItems()));
            }

            return Optional.ofNullable(restaurant);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for restaurant ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Restaurant> findByPhone(String phone) {
        logger.debug("Attempting to find restaurant by phone: '{}'", phone);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE phone = :phone", Restaurant.class);
            query.setParameter("phone", phone);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByPhone for phone '{}'", phone, e);
            return Optional.empty();
        }
    }

    @Override
    public Restaurant update(Restaurant restaurant) {
        logger.debug("Attempting to update restaurant with ID: {}", restaurant.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant updatedRestaurant = session.merge(restaurant);
            session.flush();
            transaction.commit();
            logger.info("SUCCESS: Restaurant with ID {} updated.", updatedRestaurant.getId());
            return updatedRestaurant;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for restaurant ID {}", restaurant.getId(), e);
            throw new RuntimeException("Could not update restaurant", e);
        }
    }

    @Override
    public List<Restaurant> findWithFilters(String search, List<String> keywords) {
        logger.debug("Finding vendors with filters. Search: '{}', Keywords: {}", search, keywords);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r");
            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            if (keywords != null && !keywords.isEmpty()) {
                hql.append(" LEFT JOIN r.foodItems fi JOIN fi.keywords k");
            }

            if (search != null && !search.trim().isEmpty()) {
                conditions.add("lower(r.name) LIKE :search");
                parameters.put("search", "%" + search.toLowerCase() + "%");
            }

            if (keywords != null && !keywords.isEmpty()) {
                conditions.add("k IN (:keywords)");
                parameters.put("keywords", keywords);
            }

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);
            parameters.forEach(query::setParameter);

            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findWithFilters", e);
            return Collections.emptyList();
        }
    }
}
