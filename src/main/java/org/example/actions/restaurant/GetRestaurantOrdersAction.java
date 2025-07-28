package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Order;
import org.example.model.Restaurant;
import org.example.repository.OrderRepository;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class GetRestaurantOrdersAction implements Route {
    private final Gson gson;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public GetRestaurantOrdersAction(Gson gson, RestaurantRepository restaurantRepository, OrderRepository orderRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long ownerIdFromToken = request.attribute("userId");

        // 1. Find the restaurant and verify ownership
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));
        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // 2. Get orders with filters
        List<Order> orders = orderRepository.findByRestaurantIdWithFilters(restaurantId, request.queryMap().toMap());

        // 3. Convert to DTOs
        List<OrderDTO> orderDTOS = orders.stream().map(OrderDTO::new).collect(Collectors.toList());

        // 4. Send the response
        response.status(200);
        return gson.toJson(orderDTOS);
    }
}