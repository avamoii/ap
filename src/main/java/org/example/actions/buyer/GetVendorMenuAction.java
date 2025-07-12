package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.FoodItemDTO;
import org.example.dto.RestaurantDTO;
import org.example.exception.NotFoundException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /vendors/{id} request.
 * Fetches a specific vendor's details and their full menu, structured by menu titles.
 */
public class GetVendorMenuAction implements Route {

    private final Gson gson;
    private final RestaurantRepository restaurantRepository;

    public GetVendorMenuAction(Gson gson, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));

        // 1. Find the restaurant by the given ID.
        // The repository's findById method is enhanced to fetch all necessary associations.
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + restaurantId));

        // 2. Build the complex response structure dynamically.
        Map<String, Object> responseData = new HashMap<>();

        // 2.1. Add the main vendor (restaurant) details.
        responseData.put("vendor", new RestaurantDTO(restaurant));

        // 2.2. Add a list of all menu titles.
        List<String> menuTitles = restaurant.getMenus().stream()
                .map(menu -> menu.getTitle())
                .collect(Collectors.toList());
        responseData.put("menu_titles", menuTitles);

        // 2.3. For each menu, add a key with its title and a value with its list of food items.
        restaurant.getMenus().forEach(menu -> {
            List<FoodItemDTO> foodItemDTOs = menu.getFoodItems().stream()
                    .map(FoodItemDTO::new)
                    .collect(Collectors.toList());
            // Use the menu title as the key for its items.
            responseData.put(menu.getTitle(), foodItemDTOs);
        });

        // 3. Send the successful response.
        response.status(200);
        return gson.toJson(responseData);
    }
}
