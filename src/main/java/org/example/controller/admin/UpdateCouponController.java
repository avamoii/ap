package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.CouponDTO;
import org.example.dto.UpdateCouponRequest;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class UpdateCouponController extends BaseController {
    private final CouponRepository couponRepository;

    public UpdateCouponController(Gson gson, CouponRepository couponRepository) {
        super(gson);
        this.couponRepository = couponRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String userRole = getCurrentUserRole();
        Long couponId = Long.parseLong(request.getPathParam("id"));
        UpdateCouponRequest updateRequest = gson.fromJson(request.getBody(), UpdateCouponRequest.class);

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Find the coupon to be updated (handles 404 Not Found)
        Coupon couponToUpdate = couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("Coupon not found with ID: " + couponId));

        // 3. Check for coupon code conflict if a new code is provided (handles 409 Conflict)
        if (updateRequest.getCouponCode() != null && !updateRequest.getCouponCode().equals(couponToUpdate.getCouponCode())) {
            Optional<Coupon> existingCoupon = couponRepository.findByCode(updateRequest.getCouponCode());
            if (existingCoupon.isPresent()) {
                throw new ResourceConflictException("A coupon with this code already exists.");
            }
            couponToUpdate.setCouponCode(updateRequest.getCouponCode());
        }

        // 4. Update fields if they are provided in the request
        if (updateRequest.getType() != null) {
            couponToUpdate.setType(updateRequest.getType());
        }
        if (updateRequest.getValue() != null) {
            couponToUpdate.setValue(updateRequest.getValue());
        }
        if (updateRequest.getMinPrice() != null) {
            couponToUpdate.setMinPrice(updateRequest.getMinPrice());
        }
        if (updateRequest.getUserCount() != null) {
            couponToUpdate.setUserCount(updateRequest.getUserCount());
        }
        try {
            if (updateRequest.getStartDate() != null) {
                couponToUpdate.setStartDate(LocalDate.parse(updateRequest.getStartDate()));
            }
            if (updateRequest.getEndDate() != null) {
                couponToUpdate.setEndDate(LocalDate.parse(updateRequest.getEndDate()));
            }
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date format. Please use YYYY-MM-DD.");
        }

        // 5. Save the updated coupon
        Coupon updatedCoupon = couponRepository.update(couponToUpdate);

        // 6. Return the updated coupon details
        response.status(200);
        sendJson(response, new CouponDTO(updatedCoupon));
    }
}