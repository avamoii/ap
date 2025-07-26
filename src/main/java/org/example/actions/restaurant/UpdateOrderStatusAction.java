package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.UpdateOrderStatusRequest;
import org.example.enums.OrderStatus;
import org.example.enums.VendorOrderStatus;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;
import java.util.Map;

public class UpdateOrderStatusAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public UpdateOrderStatusAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long orderId = Long.parseLong(request.params(":order_id"));
        Long ownerIdFromToken = request.attribute("userId");
        UpdateOrderStatusRequest updateRequest = gson.fromJson(request.body(), UpdateOrderStatusRequest.class);

        if (updateRequest.getStatus() == null) {
            throw new InvalidInputException("Missing required field: status is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));

        if (!order.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of the restaurant for this order.");
        }

        // --- تغییر اصلی اینجاست ---
        // منطق به‌روزرسانی وضعیت سفارش اصلاح شد
        switch (updateRequest.getStatus()) {
            case ACCEPTED:
                // وقتی سفارش تایید می‌شود، باید به وضعیت "در جستجوی پیک" برود
                order.setStatus(OrderStatus.FINDING_COURIER);
                break;
            case REJECTED:
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case SERVED:
                // این وضعیت به معنی تحویل به پیک است، پس به "در جستجوی پیک" می‌رود
                order.setStatus(OrderStatus.FINDING_COURIER);
                break;
        }
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.update(order);

        response.status(200);
        return gson.toJson(Map.of("message", "Order status changed successfully"));
    }
}