package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.CouponDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /admin/coupons request.
 * Fetches a list of all coupons, accessible only by an admin.
 */
public class ListCouponsAdminAction implements Route {

    private final Gson gson;
    private final CouponRepository couponRepository;

    public ListCouponsAdminAction(Gson gson, CouponRepository couponRepository) {
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

        // 2. Fetch all coupons from the repository.
        List<Coupon> coupons = couponRepository.findAll();

        // 3. Convert the list of Coupon entities to a list of CouponDTOs.
        List<CouponDTO> couponDTOs = coupons.stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        return gson.toJson(couponDTOs);
    }
}
