// 2. GET VENDOR MENU CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.FoodItemDTO;
import org.example.dto.RestaurantDTO;
import org.example.exception.NotFoundException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetVendorMenuController extends BaseController {
    private final RestaurantRepository restaurantRepository;

    public GetVendorMenuController(Gson gson, RestaurantRepository restaurantRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));

        // Find the restaurant by the given ID
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + restaurantId));

        // Build the complex response structure dynamically
        Map<String, Object> responseData = new HashMap<>();

        // Add the main vendor (restaurant) details
        responseData.put("vendor", new RestaurantDTO(restaurant));

        // Add a list of all menu titles
        List<String> menuTitles = restaurant.getMenus().stream()
                .map(menu -> menu.getTitle())
                .collect(Collectors.toList());
        responseData.put("menu_titles", menuTitles);

        // For each menu, add a key with its title and a value with its list of food items
        restaurant.getMenus().forEach(menu -> {
            List<FoodItemDTO> foodItemDTOs = menu.getFoodItems().stream()
                    .map(FoodItemDTO::new)
                    .collect(Collectors.toList());
            // Use the menu title as the key for its items
            responseData.put(menu.getTitle(), foodItemDTOs);
        });

        response.status(200);
        sendJson(response, responseData);
    }
}