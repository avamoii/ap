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
import java.util.Map;
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

        // Get the original query parameters from the request
        Map<String, String> queryParams = request.getQueryParams();

        // **FIX:** Convert the Map<String, String> to a Map<String, String[]>
        // This is done by wrapping each value in a new single-element String array.
        Map<String, String[]> filters = queryParams.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new String[]{entry.getValue()}
                ));

        // Get the orders using the correctly typed filters map
        List<Order> orders = orderRepository.findByCustomerIdWithFilters(customerId, filters);

        // Convert the list of entities to a list of DTOs
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        sendJson(response, orderDTOs);
    }
}