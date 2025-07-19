package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RemoveFavoriteRestaurantAction implements Route {
    private final Gson gson;
    private final UserRepository userRepository;

    public RemoveFavoriteRestaurantAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");
        Long restaurantId = Long.parseLong(request.params(":restaurantId"));

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));

        boolean removed = user.getFavoriteRestaurants().removeIf(r -> r.getId().equals(restaurantId));
        if (!removed) {
            throw new NotFoundException("Restaurant was not found in your favorites.");
        }

        userRepository.update(user);

        response.status(200);
        return gson.toJson(Map.of("message", "Removed from favorites"));
    }
}
