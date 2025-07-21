package org.example.actions.courier;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.enums.OrderStatus;
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
 * Action to handle the GET /deliveries/available request.
 * Fetches all orders that are ready for pickup by a courier.
 */
public class GetAvailableDeliveriesAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public GetAvailableDeliveriesAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");

        // 1. Check user role (handles 403 Forbidden)
        if (!UserRole.COURIER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only couriers can view available deliveries.");
        }

        // 2. Fetch all orders with the status 'FINDING_COURIER'
        // This status indicates the restaurant has prepared the order and it's waiting for pickup.
        List<Order> availableOrders = orderRepository.findByStatus(OrderStatus.FINDING_COURIER);

        // 3. Convert the list of entities to a list of DTOs
        List<OrderDTO> orderDTOs = availableOrders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response
        response.status(200);
        return gson.toJson(orderDTOs);
    }
}
