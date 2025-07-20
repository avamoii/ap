package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Rating;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public class RatingRepositoryImpl implements RatingRepository {

    private static final Logger logger = LoggerFactory.getLogger(RatingRepositoryImpl.class);

    @Override
    public Rating save(Rating rating) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(rating);
            transaction.commit();
            logger.info("SUCCESS: Rating saved with ID: {}", rating.getId());
            return rating;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for rating", e);
            throw new RuntimeException("Could not save rating", e);
        }
    }

    @Override
    public Optional<Rating> findByOrderId(Long orderId) {
        logger.debug("Attempting to find rating by order ID: {}", orderId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery("FROM Rating WHERE order.id = :orderId", Rating.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByOrderId for order ID {}", orderId, e);
            return Optional.empty();
        }
    }
    @Override
    public List<Rating> findByFoodItemId(Long foodItemId) {
        logger.debug("Finding ratings for food item ID: {}", foodItemId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery("FROM Rating WHERE foodItem.id = :foodItemId", Rating.class);
            query.setParameter("foodItemId", foodItemId);
            List<Rating> ratings = query.list();
            // Initialize associations to prevent LazyInitializationException
            for (Rating rating : ratings) {
                Hibernate.initialize(rating.getUser());
                Hibernate.initialize(rating.getFoodItem());
            }
            return ratings;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByFoodItemId for food item ID {}", foodItemId, e);
            return Collections.emptyList();
        }
    }
}
