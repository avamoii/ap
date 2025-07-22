package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.CouponDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Action to handle the GET /admin/coupons/{id} request.
 * Fetches details of a specific coupon, accessible only by an admin.
 */
public class GetCouponDetailsAdminAction implements Route {

    private final Gson gson;
    private final CouponRepository couponRepository;

    public GetCouponDetailsAdminAction(Gson gson, CouponRepository couponRepository) {
        this.gson = gson;
        this.couponRepository = couponRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Validate and parse the coupon ID from the path (handles 400 Invalid Input)
        Long couponId;
        try {
            couponId = Long.parseLong(request.params(":id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid coupon ID format.");
        }

        // 3. Find the coupon by its ID (handles 404 Not Found)
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + couponId));

        // 4. Return the coupon details as a DTO
        response.status(200);
        return gson.toJson(new CouponDTO(coupon));
    }
}
