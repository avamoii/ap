// 5. CHECK COUPON CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.CouponDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;

public class CheckCouponController extends BaseController {
    private final CouponRepository couponRepository;

    public CheckCouponController(Gson gson, CouponRepository couponRepository) {
        super(gson);
        this.couponRepository = couponRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Read query parameter (400 error handling)
        String couponCode = request.getQueryParam("coupon_code");
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new InvalidInputException("Query parameter 'coupon_code' is required.");
        }

        // Find coupon (404 error handling)
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new NotFoundException("Coupon with code '" + couponCode + "' not found."));

        // TODO: Future logic can be added here (e.g., checking expiry date or usage count)

        // Send success response
        response.status(200);
        sendJson(response, new CouponDTO(coupon));
    }
}