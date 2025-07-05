package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Implementation of UserRepository using Hibernate.
 * This class contains all the database interaction logic.
 */
public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        logger.debug("Attempting to find user by phone: '{}'", phoneNumber);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :p_number", User.class);
            query.setParameter("p_number", phoneNumber);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByPhoneNumber for phone '{}'", phoneNumber, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Attempting to find user by email: '{}'", email);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email_addr", User.class);
            query.setParameter("email_addr", email);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByEmail for email '{}'", email, e);
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        logger.debug("Attempting to save user with phone: '{}'", user.getPhoneNumber());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            session.flush();
            transaction.commit();
            logger.info("SUCCESS: User saved with ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in save method", e);
            throw new RuntimeException("Could not save user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Attempting to find user by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // session.get() is the most efficient Hibernate method to fetch by Primary Key.
            // It does not use HQL and directly generates the optimal SQL.
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for ID '{}'", id, e);
            return Optional.empty();
        }
    }
    @Override
    public User update(User user) {
        logger.debug("Attempting to update user with ID: {}", user.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // session.merge() بهترین راه برای به‌روزرسانی یک موجودیت است
            User updatedUser = session.merge(user);
            transaction.commit();
            logger.info("SUCCESS: User with ID {} updated.", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in update method for user ID {}", user.getId(), e);
            throw new RuntimeException("Could not update user", e);
        }
    }
}
