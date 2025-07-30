// 3. ADD FAVORITE RESTAURANT CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Restaurant;
import org.example.model.User;
import org.example.repository.RestaurantRepository;
import org.example.repository.UserRepository;

import java.util.Map;

public class AddFavoriteRestaurantController extends BaseController {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public AddFavoriteRestaurantController(Gson gson, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        super(gson);
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userId = getCurrentUserId();
        Long restaurantId = Long.parseLong(request.getPathParam("restaurantId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        Restaurant restaurantToAdd = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // Check if restaurant is already in favorites
        boolean alreadyExists = user.getFavoriteRestaurants().stream()
                .anyMatch(r -> r.getId().equals(restaurantId));
        if (alreadyExists) {
            throw new ResourceConflictException("Restaurant is already in your favorites.");
        }

        user.getFavoriteRestaurants().add(restaurantToAdd);
        userRepository.update(user);

        response.status(200);
        sendJson(response, Map.of("message", "Added to favorites"));
    }
}