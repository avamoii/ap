package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
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
            // از HQL برای پیدا کردن رستوران‌ها بر اساس فیلد owner.id استفاده می‌کنیم
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE owner.id = :ownerId", Restaurant.class);
            query.setParameter("ownerId", ownerId);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByOwnerId for owner ID {}", ownerId, e);
            // در صورت بروز خطا، یک لیست خالی برمی‌گردانیم
            return Collections.emptyList();
        }
    }
}