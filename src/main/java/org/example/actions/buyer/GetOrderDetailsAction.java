package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Action to handle the GET /orders/{id} request.
 * Fetches the details of a specific order and enforces access control.
 */
public class GetOrderDetailsAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public GetOrderDetailsAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long orderId = Long.parseLong(request.params(":id"));
        Long userIdFromToken = request.attribute("userId");
        String userRoleFromToken = request.attribute("userRole");

        //404
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + orderId));

        //403
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

        // 3. If access is granted, return the order details as a DTO
        response.status(200);
        return gson.toJson(new OrderDTO(order));
    }
}
