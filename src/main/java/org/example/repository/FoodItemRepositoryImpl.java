package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.FoodItem;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoodItemRepositoryImpl implements FoodItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemRepositoryImpl.class);

    @Override
    public FoodItem save(FoodItem foodItem) {
        logger.debug("Attempting to save food item: {}", foodItem.getName());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(foodItem);

            // ===> خطوط کد جدید و مهم برای حل مشکل <===
            // Explicitly initialize lazy-loaded collections/associations before the session closes
            // to prevent LazyInitializationException.
            logger.debug("Initializing associated entities for FoodItem ID: {}", foodItem.getId());
            Hibernate.initialize(foodItem.getRestaurant());
            Hibernate.initialize(foodItem.getKeywords());

            session.flush();
            transaction.commit();

            logger.info("SUCCESS: Food item saved with ID: {}", foodItem.getId());
            return foodItem;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in save method for food item {}", foodItem.getName(), e);
            throw new RuntimeException("Could not save food item", e);
        }
    }
}
