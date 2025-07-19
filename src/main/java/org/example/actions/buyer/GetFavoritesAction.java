package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.RestaurantDTO;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /favorites request.
 * Fetches the list of favorite restaurants for the currently logged-in user.
 */
public class GetFavoritesAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public GetFavoritesAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");

        // 1. Find the current user by their ID.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // 2. Get the list of favorite restaurants from the user entity.
        // 3. Convert the list of Restaurant entities to a list of RestaurantDTOs.
        List<RestaurantDTO> favoriteRestaurantDTOs = user.getFavoriteRestaurants().stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        return gson.toJson(favoriteRestaurantDTOs);
    }
}
