// ==================== COUPON MANAGEMENT CONTROLLERS ====================
package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.CouponDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ListCouponsAdminController extends BaseController {
    private final CouponRepository couponRepository;

    public ListCouponsAdminController(Gson gson, CouponRepository couponRepository) {
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

        // 2. Fetch all coupons from the repository.
        List<Coupon> coupons = couponRepository.findAll();

        // 3. Convert the list of Coupon entities to a list of CouponDTOs.
        List<CouponDTO> couponDTOs = coupons.stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        sendJson(response, couponDTOs);
    }
}