// 5. GET ORDER HISTORY CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.OrderDTO;
import org.example.model.Order;
import org.example.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetOrderHistoryController extends BaseController {
    private final OrderRepository orderRepository;

    public GetOrderHistoryController(Gson gson, OrderRepository orderRepository) {
        super(gson);
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long customerId = getCurrentUserId();

        // Get the orders with filters from the query parameters
        List<Order> orders = orderRepository.findByCustomerIdWithFilters(customerId, request.getQueryParams());

        // Convert the list of entities to a list of DTOs
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        sendJson(response, orderDTOs);
    }
}