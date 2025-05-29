package org.example.DAO;

import org.example.model.User;
import org.example.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import jakarta.persistence.NoResultException;

public class UserDao {

    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace(); // لاگ کردن خطا برای بررسی
            throw new RuntimeException("Could not save user: " + e.getMessage(), e);
        }
    }

    public User findByPhoneNumber(String phoneNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :phoneNumber", User.class);
            query.setParameter("phoneNumber", phoneNumber);
            return query.uniqueResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace(); // لاگ کردن خطا برای بررسی
            throw new RuntimeException("Error finding user by phone number: " + e.getMessage(), e);
        }
    }
}