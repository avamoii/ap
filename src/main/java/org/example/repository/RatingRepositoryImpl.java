package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Order;
import org.example.model.Rating;
import org.example.model.User;
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

            // --- راه حل نهایی و قطعی برای خطای 500 ---
            // این رویکرد بسیار مقاوم‌تر است و تمام حالات ممکن را پوشش می‌دهد.

            // 1. بررسی می‌کنیم که آبجکت‌های لازم null نباشند.
            if (rating.getOrder() == null || rating.getOrder().getId() == null) {
                throw new IllegalStateException("Rating cannot be saved without a valid Order.");
            }
            if (rating.getUser() == null || rating.getUser().getId() == null) {
                throw new IllegalStateException("Rating cannot be saved without a valid User.");
            }

            // 2. با استفاده از ID، نسخه‌های مدیریت‌شده (managed) این آبجکت‌ها را از session فعلی می‌خوانیم.
            Order managedOrder = session.get(Order.class, rating.getOrder().getId());
            User managedUser = session.get(User.class, rating.getUser().getId());

            // 3. بررسی می‌کنیم که این آبجکت‌ها در دیتابیس وجود داشته باشند.
            if (managedOrder == null) {
                throw new IllegalStateException("Associated Order with ID " + rating.getOrder().getId() + " not found in database.");
            }
            if (managedUser == null) {
                throw new IllegalStateException("Associated User with ID " + rating.getUser().getId() + " not found in database.");
            }

            // 4. آبجکت‌های مدیریت‌شده را به rating متصل می‌کنیم.
            rating.setOrder(managedOrder);
            rating.setUser(managedUser);

            // 5. حالا با اطمینان کامل، rating را ذخیره می‌کنیم.
            session.persist(rating);

            transaction.commit();
            logger.info("SUCCESS: Rating saved with ID: {}", rating.getId());
            return rating;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for rating", e);
            // این خطا حالا باید اطلاعات دقیق‌تری در لاگ سرور شما ثبت کند.
            throw new RuntimeException("Could not save rating. Check server logs for details.", e);
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
                if (rating.getFoodItem() != null) {
                    Hibernate.initialize(rating.getFoodItem());
                }
            }
            return ratings;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByFoodItemId for food item ID {}", foodItemId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Rating> findById(Long id) {
        logger.debug("Attempting to find rating by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Rating rating = session.get(Rating.class, id);
            if (rating != null) {
                // Initialize user for ownership checks
                Hibernate.initialize(rating.getUser());
                if (rating.getFoodItem() != null) {
                    Hibernate.initialize(rating.getFoodItem());
                }
            }
            return Optional.ofNullable(rating);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for rating ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Rating update(Rating rating) {
        logger.debug("Attempting to update rating with ID: {}", rating.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Rating updatedRating = session.merge(rating);
            transaction.commit();
            logger.info("SUCCESS: Rating with ID {} updated.", updatedRating.getId());
            return updatedRating;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for rating ID {}", rating.getId(), e);
            throw new RuntimeException("Could not update rating", e);
        }
    }

    @Override
    public void delete(Rating rating) {
        logger.debug("Attempting to delete rating with ID: {}", rating.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(rating);
            transaction.commit();
            logger.info("SUCCESS: Rating with ID {} deleted.", rating.getId());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in delete method for rating ID {}", rating.getId(), e);
            throw new RuntimeException("Could not delete rating", e);
        }
    }
}
