// 2. GET FAVORITES CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RestaurantDTO;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetFavoritesController extends BaseController {
    private final UserRepository userRepository;

    public GetFavoritesController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userId = getCurrentUserId();

        // Find the current user by their ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // Get the list of favorite restaurants and convert to DTOs
        List<RestaurantDTO> favoriteRestaurantDTOs = user.getFavoriteRestaurants().stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        sendJson(response, favoriteRestaurantDTOs);
    }
}