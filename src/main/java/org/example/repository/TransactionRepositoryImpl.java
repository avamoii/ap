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
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByUserId for user ID {}", userId, e);
            return new ArrayList<>();
        }
    }
}
