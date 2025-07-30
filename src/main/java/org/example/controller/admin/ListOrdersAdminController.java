// ==================== ORDER MANAGEMENT CONTROLLER ====================
package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.OrderDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Order;
import org.example.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListOrdersAdminController extends BaseController {
    private final OrderRepository orderRepository;

    public ListOrdersAdminController(Gson gson, OrderRepository orderRepository) {
        super(gson);
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String userRole = getCurrentUserRole();

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // Get the original query parameters
        Map<String, String> queryParams = request.getQueryParams();

        // **FIX:** Convert the Map<String, String> to a Map<String, String[]>
        Map<String, String[]> filters = queryParams.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new String[]{entry.getValue()}
                ));

        // 2. Fetch all orders using the correctly typed filters map
        List<Order> orders = orderRepository.findAllWithFilters(filters);

        // 3. Convert the list of Order entities to a list of OrderDTOs
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response
        response.status(200);
        sendJson(response, orderDTOs);
    }
}