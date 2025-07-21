package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            logger.info("Found {} transactions for user ID: {}", transactions.size(), userId);
            return transactions;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByUserId for user ID {}", userId, e);
            return new ArrayList<>();
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
}
