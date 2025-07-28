package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.config.HibernateUtil;
import org.example.enums.TransactionType;
import org.example.model.Order;
import org.example.model.User;
// Note: We are deliberately not importing org.example.model.Transaction to avoid naming conflicts
import org.hibernate.Hibernate;
import org.hibernate.Session;
// Note: We are deliberately not importing org.hibernate.Transaction to use the full path
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        org.hibernate.Transaction hibernateTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();
            session.persist(user);
            session.flush();
            hibernateTransaction.commit();
            logger.info("SUCCESS: User saved with ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            if (hibernateTransaction != null) hibernateTransaction.rollback();
            logger.error("CRITICAL ERROR in save method for user", e);
            throw new RuntimeException("Could not save user", e);
        }
    }

    @Override
    public User update(User user) {
        logger.debug("Attempting to update user with ID: {}", user.getId());
        org.hibernate.Transaction hibernateTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            session.flush();
            hibernateTransaction.commit();
            logger.info("SUCCESS: User with ID {} updated.", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            if (hibernateTransaction != null) hibernateTransaction.rollback();
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

    @Override
    public void processWalletPayment(Long userId, Integer amount, Long orderId) {
        org.hibernate.Transaction hibernateTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();

            User user = session.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
            if (user == null) {
                throw new IllegalStateException("User not found.");
            }

            // --- **این لاگ بسیار مهم است** ---
            logger.info("Checking wallet payment. User Balance: [{}], Order Price: [{}]", user.getWalletBalance(), amount);

            if (user.getWalletBalance() < amount) {
                logger.warn("Payment failed for user ID {}: Insufficient funds. Wallet has {}, but order requires {}.", userId, user.getWalletBalance(), amount);
                throw new IllegalStateException("Insufficient wallet balance.");
            }

            user.setWalletBalance(user.getWalletBalance() - amount);
            session.merge(user);

            org.example.model.Transaction paymentTransaction = new org.example.model.Transaction();
            paymentTransaction.setUser(user);
            paymentTransaction.setAmount(-amount);
            paymentTransaction.setType(TransactionType.PAYMENT);
            paymentTransaction.setCreatedAt(LocalDateTime.now());

            Order order = session.get(Order.class, orderId);
            if(order != null) {
                paymentTransaction.setOrder(order);
            }

            session.persist(paymentTransaction);
            hibernateTransaction.commit();
        } catch (Exception e) {
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void processWalletDeposit(Long userId, Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        org.hibernate.Transaction hibernateTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();

            User user = session.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
            if (user == null) {
                throw new IllegalStateException("User not found.");
            }

            user.setWalletBalance(user.getWalletBalance() + amount);
            session.merge(user);

            org.example.model.Transaction depositTransaction = new org.example.model.Transaction();
            depositTransaction.setUser(user);
            depositTransaction.setAmount(amount);
            depositTransaction.setType(TransactionType.DEPOSIT);
            depositTransaction.setCreatedAt(LocalDateTime.now());
            session.persist(depositTransaction);

            hibernateTransaction.commit();
        } catch (Exception e) {
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            throw new RuntimeException("Failed to process wallet deposit.", e);
        }
    }
}