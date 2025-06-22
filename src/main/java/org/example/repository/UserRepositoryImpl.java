// File: src/main/java/org/example/repository/UserRepositoryImpl.java
package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        // تمام منطق پیدا کردن کاربر که قبلا در Action بود، به اینجا منتقل می‌شود
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :phoneNumber", User.class);
            query.setParameter("phoneNumber", phoneNumber);
            // .uniqueResultOptional() به صورت خودکار یک Optional برمی‌گرداند
            return query.uniqueResultOptional();
        } catch (Exception e) {
            e.printStackTrace();
            // در صورت بروز خطا، یک Optional خالی برمی‌گردانیم
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        // تمام منطق ذخیره کردن کاربر به اینجا منتقل می‌شود
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
            e.printStackTrace();
            // در صورت بروز خطا، یک Exception پرتاب می‌کنیم تا هندلر سراسری آن را بگیرد
            throw new RuntimeException("Could not save user: " + e.getMessage(), e);
        }
    }
}