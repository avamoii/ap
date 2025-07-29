// 9. GET RESTAURANT ORDERS CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.OrderDTO;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.model.Restaurant;
import org.example.repository.OrderRepository;
import org.example.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetRestaurantOrdersController extends BaseController {
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public GetRestaurantOrdersController(Gson gson, RestaurantRepository restaurantRepository, OrderRepository orderRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        Long ownerIdFromToken = getCurrentUserId();

        // Find restaurant and verify ownership
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Get orders with filters (convert query params to map)
        List<Order> orders = orderRepository.findByRestaurantIdWithFilters(restaurantId, request.getQueryParams());

        // Convert to DTOs
        List<OrderDTO> orderDTOS = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        // Send response
        response.status(200);
        sendJson(response, orderDTOS);
    }
}