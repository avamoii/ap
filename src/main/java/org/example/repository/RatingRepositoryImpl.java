package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Rating;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
}
