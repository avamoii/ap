package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;

public class RestaurantRepositoryImpl implements RestaurantRepository {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantRepositoryImpl.class);

    @Override
    public Restaurant save(Restaurant restaurant) {
        logger.debug("Attempting to save restaurant: {}", restaurant.getName());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(restaurant);

            // Add flush to ensure data is written to the DB before the transaction commits.
            session.flush();

            transaction.commit();
            logger.info("SUCCESS: Restaurant saved with ID: {}", restaurant.getId());
            return restaurant;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in save method for restaurant {}", restaurant.getName(), e);
            throw new RuntimeException("Could not save restaurant", e);
        }
    }

    @Override
    public List<Restaurant> findByOwnerId(Long ownerId) {
        logger.debug("Attempting to find restaurants for owner ID: {}", ownerId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Using HQL to find restaurants based on the owner.id field
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE owner.id = :ownerId", Restaurant.class);
            query.setParameter("ownerId", ownerId);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByOwnerId for owner ID {}", ownerId, e);
            // Return an empty list in case of an error
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        logger.debug("Attempting to find restaurant by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, id);

            // This is crucial to prevent LazyInitializationException
            if (restaurant != null) {
                // Explicitly initialize the owner association before the session closes
                Hibernate.initialize(restaurant.getOwner());
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
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in update method for restaurant ID {}", restaurant.getId(), e);
            throw new RuntimeException("Could not update restaurant", e);
        }
    }
}
