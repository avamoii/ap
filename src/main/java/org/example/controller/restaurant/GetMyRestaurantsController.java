// 2. GET MY RESTAURANTS CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RestaurantDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetMyRestaurantsController extends BaseController {
    private final RestaurantRepository restaurantRepository;

    public GetMyRestaurantsController(Gson gson, RestaurantRepository restaurantRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Check user role (403)
        String userRole = getCurrentUserRole();
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can view their restaurants.");
        }

        // Get owner ID from JWT context
        Long ownerId = getCurrentUserId();

        // Fetch restaurants from repository
        List<Restaurant> myRestaurants = restaurantRepository.findByOwnerId(ownerId);

        // Convert to DTOs
        List<RestaurantDTO> restaurantDTOS = myRestaurants.stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());

        // Send success response
        response.status(200);
        sendJson(response, restaurantDTOS);
    }
}