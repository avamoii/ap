// 4. REMOVE FAVORITE RESTAURANT CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.Map;

public class RemoveFavoriteRestaurantController extends BaseController {
    private final UserRepository userRepository;

    public RemoveFavoriteRestaurantController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userId = getCurrentUserId();
        Long restaurantId = Long.parseLong(request.getPathParam("restaurantId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // Remove restaurant from favorites
        boolean removed = user.getFavoriteRestaurants().removeIf(r -> r.getId().equals(restaurantId));
        if (!removed) {
            throw new NotFoundException("Restaurant was not found in your favorites.");
        }

        userRepository.update(user);

        response.status(200);
        sendJson(response, Map.of("message", "Removed from favorites"));
    }
}