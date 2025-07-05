package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestaurantRepositoryImpl implements RestaurantRepository {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantRepositoryImpl.class);

    @Override
    public Restaurant save(Restaurant restaurant) {
        logger.debug("Attempting to save restaurant: {}", restaurant.getName());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(restaurant);
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
}