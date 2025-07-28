package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Transaction;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.enums.TransactionType;

import java.util.ArrayList;
import java.util.*;

public class TransactionRepositoryImpl implements TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRepositoryImpl.class);

    @Override
    public List<Transaction> findByUserId(Long userId) {
        logger.debug("Finding transaction history for user ID: {}", userId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "FROM Transaction WHERE user.id = :userId ORDER BY createdAt DESC", Transaction.class);
            query.setParameter("userId", userId);
            List<Transaction> transactions = query.list();

            // این بخش از قبل صحیح است و اطلاعات لازم را واکشی می‌کند
            for (Transaction transaction : transactions) {
                Hibernate.initialize(transaction.getUser());
                if (transaction.getOrder() != null) {
                    Hibernate.initialize(transaction.getOrder());
                }
            }

            logger.info("Found {} transactions for user ID: {}", transactions.size(), userId);
            return transactions;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByUserId for user ID {}", userId, e);
            throw new RuntimeException("Could not fetch transaction history", e);
        }
    }

    @Override
    public Transaction save(Transaction transaction) {
        logger.debug("Attempting to save transaction for user ID: {}", transaction.getUser().getId());
        org.hibernate.Transaction hibernateTransaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();
            session.persist(transaction);
            session.flush(); // Ensures data is sent to the DB
            hibernateTransaction.commit();
            logger.info("SUCCESS: Transaction saved with ID: {}", transaction.getId());
            return transaction;
        } catch (Exception e) {
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            logger.error("CRITICAL ERROR in save method for transaction", e);
            throw new RuntimeException("Could not save transaction", e);
        }
    }
    @Override
    public List<Transaction> findAllWithFilters(Map<String, String[]> filters) {
        logger.debug("Finding all transactions with filters: {}", filters);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT t FROM Transaction t JOIN t.user u");
            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            if (filters.containsKey("user") && filters.get("user")[0] != null && !filters.get("user")[0].isEmpty()) {
                conditions.add("lower(u.firstName || ' ' || u.lastName) LIKE :userName");
                parameters.put("userName", "%" + filters.get("user")[0].toLowerCase() + "%");
            }
            if (filters.containsKey("method") && filters.get("method")[0] != null && !filters.get("method")[0].isEmpty()) {
                try {
                    conditions.add("t.type = :type");
                    parameters.put("type", TransactionType.valueOf(filters.get("method")[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid transaction type provided in filter: {}", filters.get("method")[0]);
                    return new ArrayList<>();
                }
            }

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            hql.append(" ORDER BY t.createdAt DESC");

            Query<Transaction> query = session.createQuery(hql.toString(), Transaction.class);
            parameters.forEach(query::setParameter);

            List<Transaction> transactions = query.list();

            // --- **تغییر اصلی اینجاست** ---
            // قبل از بازگرداندن لیست، اطلاعات کاربر هر تراکنش را به طور کامل بارگذاری می‌کنیم
            for (Transaction transaction : transactions) {
                Hibernate.initialize(transaction.getUser());
            }

            return transactions;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findAllWithFilters for transactions", e);
            return new ArrayList<>();
        }
    }
}