package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Coupon;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
}