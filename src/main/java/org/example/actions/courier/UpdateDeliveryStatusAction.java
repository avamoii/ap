package org.example.actions.courier;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.dto.UpdateDeliveryStatusRequest;
import org.example.enums.OrderStatus;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Order;
import org.example.model.User;
import org.example.repository.OrderRepository;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;
import java.util.Map;

public class UpdateDeliveryStatusAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public UpdateDeliveryStatusAction(Gson gson, OrderRepository orderRepository, UserRepository userRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long orderId = Long.parseLong(request.params(":order_id"));
        Long courierIdFromToken = request.attribute("userId");
        String userRole = request.attribute("userRole");
        UpdateDeliveryStatusRequest updateRequest = gson.fromJson(request.body(), UpdateDeliveryStatusRequest.class);

        // ۱. بررسی نقش کاربر (برای خطای 403)
        if (!UserRole.COURIER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only couriers can change delivery status.");
        }

        // ۲. اعتبارسنجی ورودی (برای خطای 400)
        if (updateRequest.getStatus() == null) {
            throw new InvalidInputException("Missing required field: status is required.");
        }

        // ۳. پیدا کردن سفارش (برای خطای 404)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));

        User courier = userRepository.findById(courierIdFromToken)
                .orElseThrow(() -> new NotFoundException("Courier user not found."));

        // ۴. منطق تغییر وضعیت
        switch (updateRequest.getStatus()) {
            case ACCEPTED:
                // یک پیک فقط می‌تواند سفارشی را قبول کند که منتظر پیک است و هنوز پیکی به آن اختصاص داده نشده
                if (order.getStatus() != OrderStatus.FINDING_COURIER) {
                    throw new ForbiddenException("This order is not available for pickup.");
                }
                if (order.getCourier() != null) {
                    throw new ResourceConflictException("Delivery already assigned to another courier.");
                }
                order.setCourier(courier);
                order.setStatus(OrderStatus.ON_THE_WAY);
                break;

            case RECEIVED:
                // این وضعیت در API شما تعریف شده اما در Enum اصلی ما نیست. می‌توانیم آن را نادیده بگیریم یا به یک وضعیت موجود مپ کنیم.
                // فعلاً فرض می‌کنیم این وضعیت همان ON_THE_WAY است.
                if (order.getCourier() == null || !order.getCourier().getId().equals(courierIdFromToken)) {
                    throw new ForbiddenException("Access denied. You are not the courier for this order.");
                }
                order.setStatus(OrderStatus.ON_THE_WAY);
                break;

            case DELIVERED:
                // یک پیک فقط می‌تواند سفارشی را تحویل دهد که به خودش اختصاص داده شده
                if (order.getCourier() == null || !order.getCourier().getId().equals(courierIdFromToken)) {
                    throw new ForbiddenException("Access denied. You are not the courier for this order.");
                }
                order.setStatus(OrderStatus.COMPLETED);
                break;
        }
        order.setUpdatedAt(LocalDateTime.now());

        // ۵. ذخیره تغییرات
        Order updatedOrder = orderRepository.update(order);

        // ۶. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(Map.of(
                "message", "Changed status successfully",
                "order", new OrderDTO(updatedOrder)
        ));
    }
}
