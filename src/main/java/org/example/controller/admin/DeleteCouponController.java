package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;

public class DeleteCouponController extends BaseController {
    private final CouponRepository couponRepository;

    public DeleteCouponController(Gson gson, CouponRepository couponRepository) {
        super(gson);
        this.couponRepository = couponRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String userRole = getCurrentUserRole();

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Validate and parse the coupon ID from the path (handles 400 Invalid Input)
        Long couponId;
        try {
            couponId = Long.parseLong(request.getPathParam("id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid coupon ID format.");
        }

        // 3. Find the coupon to be deleted (handles 404 Not Found)
        Coupon couponToDelete = couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + couponId));

        // 4. Delete the coupon
        couponRepository.delete(couponToDelete);

        // 5. Return a success message
        response.status(200);
        response.body(""); // The API specifies no response body on success
    }
}