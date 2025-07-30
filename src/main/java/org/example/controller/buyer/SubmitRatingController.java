// 1. SUBMIT RATING CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.SubmitRatingRequest;
import org.example.enums.OrderStatus;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Order;
import org.example.model.Rating;
import org.example.repository.OrderRepository;
import org.example.repository.RatingRepository;

import java.time.LocalDateTime;

public class SubmitRatingController extends BaseController {
    private final OrderRepository orderRepository;
    private final RatingRepository ratingRepository;

    public SubmitRatingController(Gson gson, OrderRepository orderRepository, RatingRepository ratingRepository) {
        super(gson);
        this.orderRepository = orderRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userIdFromToken = getCurrentUserId();
        SubmitRatingRequest ratingRequest = gson.fromJson(request.getBody(), SubmitRatingRequest.class);

        // Input validation
        if (ratingRequest.getOrderId() == null || ratingRequest.getRating() == null || ratingRequest.getComment() == null) {
            throw new InvalidInputException("Missing required fields: orderId, rating, and comment are required.");
        }
        if (ratingRequest.getRating() < 1 || ratingRequest.getRating() > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }

        // Find order
        Order order = orderRepository.findById(ratingRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        // Check order ownership
        if (!order.getCustomer().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only rate your own orders.");
        }

        // Check order status
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResourceConflictException("You can only rate completed orders.");
        }

        // Check if rating already exists
        ratingRepository.findByOrderId(order.getId()).ifPresent(r -> {
            throw new ResourceConflictException("A rating for this order has already been submitted.");
        });

        // Create and save new rating
        Rating newRating = new Rating();
        newRating.setOrder(order);
        newRating.setUser(order.getCustomer());
        newRating.setRating(ratingRequest.getRating());
        newRating.setComment(ratingRequest.getComment());
        newRating.setImageBase64(ratingRequest.getImageBase64());
        newRating.setCreatedAt(LocalDateTime.now());

        ratingRepository.save(newRating);

        // Send success response with empty body
        response.status(200);
        response.body("");
    }
}