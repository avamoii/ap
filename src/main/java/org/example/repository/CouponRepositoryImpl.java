package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Coupon;
import org.example.model.Order; // <-- ایمپورت جدید
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponRepositoryImpl implements CouponRepository {
    private static final Logger logger = LoggerFactory.getLogger(CouponRepositoryImpl.class);

    @Override
    public Optional<Coupon> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Coupon.class, id));
        }
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon WHERE couponCode = :code", Coupon.class);
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Coupon> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Coupon", Coupon.class).list();
        }
    }

    @Override
    public Coupon save(Coupon coupon) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(coupon);
            transaction.commit();
            logger.info("SUCCESS: Coupon saved with ID: {}", coupon.getId());
            return coupon;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for coupon", e);
            throw new RuntimeException("Could not save coupon", e);
        }
    }

    @Override
    public Coupon update(Coupon coupon) {
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

    // --- **متد اصلاح شده اینجاست** ---
    @Override
    public void delete(Coupon coupon) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // مرحله ۱: تمام سفارش‌هایی که از این کوپن استفاده می‌کنند را پیدا کن
            List<Order> ordersToUpdate = session.createQuery("FROM Order WHERE coupon.id = :couponId", Order.class)
                    .setParameter("couponId", coupon.getId())
                    .list();

            // مرحله ۲: ارتباط این سفارش‌ها با کوپن را قطع کن (coupon_id را null کن)
            for (Order order : ordersToUpdate) {
                order.setCoupon(null);
                session.merge(order);
            }

            // برای اطمینان از اعمال تغییرات قبل از حذف
            session.flush();

            // مرحله ۳: حالا که هیچ سفارشی به کوپن وصل نیست، آن را با خیال راحت حذف کن
            session.remove(coupon);

            transaction.commit();
            logger.info("SUCCESS: Coupon with ID {} deleted and order references updated.", coupon.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("CRITICAL ERROR in delete method for coupon ID {}", coupon.getId(), e);
            throw new RuntimeException("Could not delete coupon", e);
        }
    }
}