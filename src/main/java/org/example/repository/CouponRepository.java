package org.example.repository;

import org.example.model.Coupon;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByCode(String code);
    Optional<Coupon> findById(Long id);
    Coupon update(Coupon coupon);
    List<Coupon> findAll();
    Coupon save(Coupon coupon);
    void delete(Coupon coupon);
}
