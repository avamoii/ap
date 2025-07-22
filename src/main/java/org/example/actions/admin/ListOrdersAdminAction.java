package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /admin/orders request.
 * Fetches a list of all orders with optional filters, accessible only by an admin.
 */
public class ListOrdersAdminAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public ListOrdersAdminAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Fetch all orders from the repository, passing the query parameters as filters.
        List<Order> orders = orderRepository.findAllWithFilters(request.queryMap().toMap());

        // 3. Convert the list of Order entities to a list of OrderDTOs.
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        return gson.toJson(orderDTOs);
    }
}
