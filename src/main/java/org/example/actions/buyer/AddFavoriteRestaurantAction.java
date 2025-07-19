package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Restaurant;
import org.example.model.User;
import org.example.repository.RestaurantRepository;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class AddFavoriteRestaurantAction implements Route {
    private final Gson gson;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public AddFavoriteRestaurantAction(Gson gson, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");
        Long restaurantId = Long.parseLong(request.params(":restaurantId"));

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
        Restaurant restaurantToAdd = restaurantRepository.findById(restaurantId).orElseThrow(() -> new NotFoundException("Restaurant not found."));

        boolean alreadyExists = user.getFavoriteRestaurants().stream().anyMatch(r -> r.getId().equals(restaurantId));
        if (alreadyExists) {
            throw new ResourceConflictException("Restaurant is already in your favorites.");
        }

        user.getFavoriteRestaurants().add(restaurantToAdd);
        userRepository.update(user);

        response.status(200);
        return gson.toJson(Map.of("message", "Added to favorites"));
    }
}
