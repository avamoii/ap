package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /orders/history request.
 * Fetches the order history for the currently logged-in user.
 */
public class GetOrderHistoryAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;

    public GetOrderHistoryAction(Gson gson, OrderRepository orderRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long customerId = request.attribute("userId");

        // 1. Get the orders with filters from the query parameters
        List<Order> orders = orderRepository.findByCustomerIdWithFilters(customerId, request.queryMap().toMap());

        // 2. Convert the list of entities to a list of DTOs
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // 3. Send the response
        response.status(200);
        return gson.toJson(orderDTOs);
    }
}
