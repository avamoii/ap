// 6. GET ORDER DETAILS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.OrderDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.repository.OrderRepository;

public class GetOrderDetailsController extends BaseController {
    private final OrderRepository orderRepository;

    public GetOrderDetailsController(Gson gson, OrderRepository orderRepository) {
        super(gson);
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long orderId = Long.parseLong(request.getPathParam("id"));
        Long userIdFromToken = getCurrentUserId();
        String userRoleFromToken = getCurrentUserRole();

        // Find order (404 if not found)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + orderId));

        // Check access permissions (403 if not allowed)
        boolean isAllowed = false;

        // Allow if the user is an ADMIN
        if (UserRole.ADMIN.toString().equals(userRoleFromToken)) {
            isAllowed = true;
        }
        // Allow if the user is the customer who placed the order
        else if (order.getCustomer().getId().equals(userIdFromToken)) {
            isAllowed = true;
        }
        // Allow if the user is the owner of the restaurant for this order
        else if (order.getRestaurant().getOwner().getId().equals(userIdFromToken)) {
            isAllowed = true;
        }
        // Allow if the user is the courier assigned to this order
        else if (order.getCourier() != null && order.getCourier().getId().equals(userIdFromToken)) {
            isAllowed = true;
        }

        if (!isAllowed) {
            throw new ForbiddenException("Access denied. You are not authorized to view this order.");
        }

        // Return the order details as a DTO
        response.status(200);
        sendJson(response, new OrderDTO(order));
    }
}