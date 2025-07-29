// 10. UPDATE ORDER STATUS CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.UpdateOrderStatusRequest;
import org.example.enums.OrderStatus;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Map;

public class UpdateOrderStatusController extends BaseController {
    private final OrderRepository orderRepository;

    public UpdateOrderStatusController(Gson gson, OrderRepository orderRepository) {
        super(gson);
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long orderId = Long.parseLong(request.getPathParam("order_id"));
        Long ownerIdFromToken = getCurrentUserId();
        UpdateOrderStatusRequest updateRequest = gson.fromJson(request.getBody(), UpdateOrderStatusRequest.class);

        // Validate request (400)
        if (updateRequest.getStatus() == null) {
            throw new InvalidInputException("Missing required field: status is required.");
        }

        // Find order (404)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));

        // Check ownership (403)
        if (!order.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of the restaurant for this order.");
        }

        // Update order status based on vendor status
        switch (updateRequest.getStatus()) {
            case ACCEPTED:
                // When order is accepted, it should go to "finding courier" status
                order.setStatus(OrderStatus.FINDING_COURIER);
                break;
            case REJECTED:
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case SERVED:
                // When served to courier, it goes to "finding courier" status
                order.setStatus(OrderStatus.FINDING_COURIER);
                break;
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(order);

        // Send success response
        response.status(200);
        sendJson(response, Map.of("message", "Order status changed successfully"));
    }
}