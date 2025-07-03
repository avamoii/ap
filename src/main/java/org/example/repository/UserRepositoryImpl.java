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
        // Logging to see what's happening
        System.out.println("--- findByPhoneNumber ---");
        System.out.println("Attempting to find user with phone: '" + phoneNumber + "'");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE phoneNumber = :p_number", User.class);
            query.setParameter("p_number", phoneNumber);

            Optional<User> userOptional = query.uniqueResultOptional();

            // More logging to confirm the result
            if (userOptional.isPresent()) {
                System.out.println("SUCCESS: User found with ID: " + userOptional.get().getId());
            } else {
                System.out.println("FAILURE: User with phone '" + phoneNumber + "' was NOT found in the database.");
            }

            return userOptional;
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in findByPhoneNumber: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email_addr", User.class);
            query.setParameter("email_addr", email);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        System.out.println("--- save ---");
        System.out.println("Attempting to save user with phone: '" + user.getPhoneNumber() + "'");
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            // Explicitly flush the session to force synchronization with the database
            System.out.println("Flushing session...");
            session.flush();

            // Commit the transaction
            System.out.println("Committing transaction...");
            transaction.commit();

            System.out.println("SUCCESS: User saved with ID: " + user.getId());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                System.err.println("Transaction is not null, rolling back...");
                transaction.rollback();
            }
            System.err.println("CRITICAL ERROR in save: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not save user", e);
        }
    }
}
