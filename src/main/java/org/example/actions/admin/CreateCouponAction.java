package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.CouponDTO;
import org.example.dto.CreateCouponRequest;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.ResourceConflictException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Action to handle the POST /admin/coupons request.
 * Creates a new coupon, accessible only by an admin.
 */
public class CreateCouponAction implements Route {

    private final Gson gson;
    private final CouponRepository couponRepository;

    public CreateCouponAction(Gson gson, CouponRepository couponRepository) {
        this.gson = gson;
        this.couponRepository = couponRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");
        CreateCouponRequest createRequest = gson.fromJson(request.body(), CreateCouponRequest.class);

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Validate required fields (handles 400 Invalid Input)
        if (createRequest.getCouponCode() == null || createRequest.getType() == null || createRequest.getValue() == null) {
            throw new InvalidInputException("Missing required fields: coupon_code, type, and value are required.");
        }

        // 3. Check for duplicate coupon code (handles 409 Conflict)
        couponRepository.findByCode(createRequest.getCouponCode()).ifPresent(c -> {
            throw new ResourceConflictException("A coupon with this code already exists.");
        });

        // 4. Create and populate the new Coupon entity
        Coupon newCoupon = new Coupon();
        newCoupon.setCouponCode(createRequest.getCouponCode());
        newCoupon.setType(createRequest.getType());
        newCoupon.setValue(createRequest.getValue());
        newCoupon.setMinPrice(createRequest.getMinPrice());
        newCoupon.setUserCount(createRequest.getUserCount());

        // 5. Parse date strings safely
        try {
            if (createRequest.getStartDate() != null) {
                newCoupon.setStartDate(LocalDate.parse(createRequest.getStartDate()));
            }
            if (createRequest.getEndDate() != null) {
                newCoupon.setEndDate(LocalDate.parse(createRequest.getEndDate()));
            }
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date format. Please use YYYY-MM-DD.");
        }

        // 6. Save the new coupon
        Coupon savedCoupon = couponRepository.save(newCoupon);

        // 7. Return the created coupon details
        response.status(201); // Created
        return gson.toJson(new CouponDTO(savedCoupon));
    }
}
