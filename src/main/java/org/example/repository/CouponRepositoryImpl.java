package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Coupon;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CouponRepositoryImpl implements CouponRepository {

    private static final Logger logger = LoggerFactory.getLogger(CouponRepositoryImpl.class);

    @Override
    public Optional<Coupon> findByCode(String code) {
        logger.debug("Attempting to find coupon by code: {}", code);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon WHERE couponCode = :code", Coupon.class);
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByCode for coupon code {}", code, e);
            return Optional.empty();
        }
    }
    @Override
    public Optional<Coupon> findById(Long id) {
        logger.debug("Attempting to find coupon by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Coupon.class, id));
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findById for coupon ID {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Coupon update(Coupon coupon) {
        logger.debug("Attempting to update coupon with ID: {}", coupon.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Coupon updatedCoupon = session.merge(coupon);
            transaction.commit();
            logger.info("SUCCESS: Coupon with ID {} updated.", updatedCoupon.getId());
            return updatedCoupon;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for coupon ID {}", coupon.getId(), e);
            throw new RuntimeException("Could not update coupon", e);
        }
    }
    @Override
    public List<Coupon> findAll() {
        logger.debug("Attempting to find all coupons");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon", Coupon.class);
            return query.list();
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findAll coupons", e);
            return Collections.emptyList();
        }
    }
    @Override
    public Coupon save(Coupon coupon) {
        logger.debug("Attempting to save coupon with code: {}", coupon.getCouponCode());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(coupon);
            transaction.commit();
            logger.info("SUCCESS: Coupon saved with ID: {}", coupon.getId());
            return coupon;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for coupon {}", coupon.getCouponCode(), e);
            throw new RuntimeException("Could not save coupon", e);
        }
    }
}