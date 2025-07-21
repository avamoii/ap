package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.User;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    public Optional<User> findById(Long id) {
        logger.debug("Attempting to find user by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                // Initialize the list of favorite restaurants to prevent LazyInitializationException
                Hibernate.initialize(user.getFavoriteRestaurants());
                logger.info("SUCCESS: User found with ID: {}", id);
            } else {
                logger.warn("FAILURE: User with ID '{}' was NOT found.", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for ID '{}'", id, e);
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
            session.flush(); // Ensures data is sent to the DB
            transaction.commit();
            logger.info("SUCCESS: User saved with ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for user", e);
            throw new RuntimeException("Could not save user", e);
        }
    }

    @Override
    public User update(User user) {
        logger.debug("Attempting to update user with ID: {}", user.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            session.flush(); // Ensures data is sent to the DB
            transaction.commit();
            logger.info("SUCCESS: User with ID {} updated.", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for user ID {}", user.getId(), e);
            throw new RuntimeException("Could not update user", e);
        }
    }
    @Override
    public List<User> findAll() {
        logger.debug("Attempting to find all users");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findAll users", e);
            return Collections.emptyList();
        }
    }
}
