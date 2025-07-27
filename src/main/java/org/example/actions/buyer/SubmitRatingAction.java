package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.SubmitRatingRequest;
import org.example.enums.OrderStatus;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.FoodItem;
import org.example.model.Order;
import org.example.model.Rating;
import org.example.repository.FoodItemRepository;
import org.example.repository.OrderRepository;
import org.example.repository.RatingRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;

public class SubmitRatingAction implements Route {
    private final Gson gson;
    private final OrderRepository orderRepository;
    private final RatingRepository ratingRepository;
    private final FoodItemRepository foodItemRepository;

    public SubmitRatingAction(Gson gson, OrderRepository orderRepository, RatingRepository ratingRepository,
            FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
        this.ratingRepository = ratingRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userIdFromToken = request.attribute("userId");
        SubmitRatingRequest ratingRequest = gson.fromJson(request.body(), SubmitRatingRequest.class);

        // ۱. اعتبارسنجی ورودی
        if (ratingRequest.getOrderId() == null || ratingRequest.getRating() == null || ratingRequest.getComment() == null) {
            throw new InvalidInputException("Missing required fields: orderId, rating, and comment are required.");
        }
        if (ratingRequest.getRating() < 1 || ratingRequest.getRating() > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }

        // ۲. پیدا کردن سفارش
        Order order = orderRepository.findById(ratingRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        // ۳. بررسی مالکیت سفارش
        if (!order.getCustomer().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only rate your own orders.");
        }

        // ۴. بررسی وضعیت سفارش
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResourceConflictException("You can only rate completed orders.");
        }

        // ۵. بررسی تکراری بودن امتیاز
        ratingRepository.findByOrderId(order.getId()).ifPresent(r -> {
            throw new ResourceConflictException("A rating for this order has already been submitted.");
        });

        // ۶. ساختن و ذخیره امتیاز جدید
        Rating newRating = new Rating();
        newRating.setOrder(order);
        newRating.setUser(order.getCustomer());
        newRating.setRating(ratingRequest.getRating());
        newRating.setComment(ratingRequest.getComment());
        newRating.setImageBase64(ratingRequest.getImageBase64());
        newRating.setCreatedAt(LocalDateTime.now());

        if (ratingRequest.getFoodItemId() != null) {
            FoodItem foodItem = foodItemRepository.findById(ratingRequest.getFoodItemId())
                    .orElseThrow(() -> new NotFoundException("FoodItem not found."));

            boolean itemInOrder = order.getItems().stream()
                    .anyMatch(item -> item.getId().equals(foodItem.getId()));
            if (!itemInOrder) {
                throw new InvalidInputException("The specified food item is not part of this order.");
            }
            newRating.setFoodItem(foodItem);
        }

        ratingRepository.save(newRating);

        // ۷. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return ""; // طبق API، بدنه پاسخ خالی است
    }
}
