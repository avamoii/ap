package org.example.actions.courier;

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
 * Action to handle the GET /deliveries/history request.
 * Fetches the delivery history for the currently logged-in courier.
 */
public class GetDeliveryHistoryAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public GetDeliveryHistoryAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long courierId = request.attribute("userId");
        String userRole = request.attribute("userRole");

        // 1. Check user role (handles 403 Forbidden)
        if (!UserRole.COURIER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only couriers can view their delivery history.");
        }

        // 2. Get the orders with filters from the query parameters
        List<Order> orders = orderRepository.findByCourierIdWithFilters(courierId, request.queryMap().toMap());

        // 3. Convert the list of entities to a list of DTOs
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response
        response.status(200);
        return gson.toJson(orderDTOs);
    }
}
