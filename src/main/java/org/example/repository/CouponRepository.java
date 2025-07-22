package org.example.repository;

import org.example.model.Coupon;
import java.util.*;

public interface CouponRepository {
    /**
     * Finds a coupon by its unique code.
     * @param code The coupon code to search for.
     * @return An Optional containing the coupon if found.
     */
    Optional<Coupon> findByCode(String code);

    /**
     * Finds a coupon by its unique ID.
     * @param id The ID of the coupon to find.
     * @return An Optional containing the coupon if found.
     */
    Optional<Coupon> findById(Long id);

    /**
     * Updates an existing coupon.
     * @param coupon The coupon entity with updated information.
     * @return The updated coupon entity.
     */
    Coupon update(Coupon coupon);
    List<Coupon> findAll();
    Coupon save(Coupon coupon);
}
