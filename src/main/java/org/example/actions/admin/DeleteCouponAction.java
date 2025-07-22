package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Action to handle the DELETE /admin/coupons/{id} request.
 * Deletes a coupon, accessible only by an admin.
 */
public class DeleteCouponAction implements Route {

    private final Gson gson;
    private final CouponRepository couponRepository;

    public DeleteCouponAction(Gson gson, CouponRepository couponRepository) {
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

        // 3. Find the coupon to be deleted (handles 404 Not Found)
        Coupon couponToDelete = couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + couponId));

        // 4. Delete the coupon
        couponRepository.delete(couponToDelete);

        // 5. Return a success message
        response.status(200);
        return ""; // The API specifies no response body on success
    }
}
