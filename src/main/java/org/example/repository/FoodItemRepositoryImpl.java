package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.FoodItem;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of FoodItemRepository using Hibernate.
 * This class contains all the database interaction logic for FoodItem entities.
 */
public class FoodItemRepositoryImpl implements FoodItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemRepositoryImpl.class);

    @Override
    public FoodItem save(FoodItem foodItem) {
        logger.debug("Attempting to save food item: {}", foodItem.getName());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(foodItem);

            // Explicitly initialize lazy-loaded associations before the session closes
            // to prevent LazyInitializationException when the DTO is created later.
            logger.debug("Initializing associated entities for new FoodItem");
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

    @Override
    public Optional<FoodItem> findById(Long id) {
        logger.debug("Attempting to find food item by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                // This is crucial to prevent LazyInitializationException in the Action layer.
                // We explicitly initialize the associations we know will be needed.
                Hibernate.initialize(foodItem.getRestaurant());
                Hibernate.initialize(foodItem.getRestaurant().getOwner());
                Hibernate.initialize(foodItem.getKeywords());
            }
            return Optional.ofNullable(foodItem);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for food item ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public FoodItem update(FoodItem foodItem) {
        logger.debug("Attempting to update food item with ID: {}", foodItem.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FoodItem updatedFoodItem = session.merge(foodItem);
            session.flush();
            transaction.commit();
            logger.info("SUCCESS: Food item with ID {} updated.", updatedFoodItem.getId());
            return updatedFoodItem;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in update method for food item ID {}", foodItem.getId(), e);
            throw new RuntimeException("Could not update food item", e);
        }
    }
    @Override
    public void delete(FoodItem foodItem) {
        logger.debug("Attempting to delete food item with ID: {}", foodItem.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Use session.remove() for deletion. It's the modern equivalent of session.delete().
            session.remove(foodItem);
            transaction.commit();
            logger.info("SUCCESS: Food item with ID {} deleted.", foodItem.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in delete method for food item ID {}", foodItem.getId(), e);
            throw new RuntimeException("Could not delete food item", e);
        }
    }
    @Override
    public List<FoodItem> findWithFilters(String search, Integer maxPrice, List<String> keywords) {
        logger.debug("Finding food items with filters. Search: '{}', Max Price: {}, Keywords: {}", search, maxPrice, keywords);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT fi FROM FoodItem fi");
            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            // A JOIN is only necessary if we are filtering by keywords.
            if (keywords != null && !keywords.isEmpty()) {
                hql.append(" JOIN fi.keywords k");
            }

            // Add search condition for item's name or description.
            if (search != null && !search.trim().isEmpty()) {
                conditions.add("(lower(fi.name) LIKE :search OR lower(fi.description) LIKE :search)");
                parameters.put("search", "%" + search.toLowerCase() + "%");
            }

            // Add max price condition.
            if (maxPrice != null) {
                conditions.add("fi.price == :maxPrice");
                parameters.put("maxPrice", maxPrice);
            }

            // Add keyword condition.
            if (keywords != null && !keywords.isEmpty()) {
                conditions.add("k IN (:keywords)");
                parameters.put("keywords", keywords);
            }

            // Append WHERE clause if any conditions exist.
            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            Query<FoodItem> query = session.createQuery(hql.toString(), FoodItem.class);
            parameters.forEach(query::setParameter);

            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findWithFilters for food items", e);
            return Collections.emptyList();
        }
    }
}


