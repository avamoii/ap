package org.example.repository;

import org.example.model.Coupon;
import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByCode(String code);
}