// 4. ADD FOOD ITEM CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.AddFoodItemRequest;
import org.example.dto.FoodItemDTO;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.model.Menu;
import org.example.model.Restaurant;
import org.example.repository.FoodItemRepository;
import org.example.repository.MenuRepository;
import org.example.repository.RestaurantRepository;

public class AddFoodItemController extends BaseController {
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final MenuRepository menuRepository;

    public AddFoodItemController(Gson gson, RestaurantRepository restaurantRepository,
                                 FoodItemRepository foodItemRepository, MenuRepository menuRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        String menuTitle = request.getPathParam("title");
        Long ownerIdFromToken = getCurrentUserId();
        AddFoodItemRequest addRequest = gson.fromJson(request.getBody(), AddFoodItemRequest.class);

        // Validate required fields (400)
        if (addRequest.getName() == null || addRequest.getDescription() == null ||
                addRequest.getPrice() == null || addRequest.getSupply() == null ||
                addRequest.getKeywords() == null || addRequest.getKeywords().isEmpty()) {
            throw new InvalidInputException("Missing required fields.");
        }

        // Find restaurant (404)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // Check ownership (403)
        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied.");
        }

        // Find the correct menu (404)
        Menu menu = menuRepository.findByRestaurantIdAndTitle(restaurantId, menuTitle)
                .orElseThrow(() -> new NotFoundException("Menu with title '" + menuTitle + "' not found."));

        // Create new food item
        FoodItem newFoodItem = new FoodItem();
        newFoodItem.setName(addRequest.getName());
        newFoodItem.setDescription(addRequest.getDescription());
        newFoodItem.setPrice(addRequest.getPrice());
        newFoodItem.setSupply(addRequest.getSupply());
        newFoodItem.setImageBase64(addRequest.getImageBase64());
        newFoodItem.setKeywords(addRequest.getKeywords());
        newFoodItem.setRestaurant(restaurant);

        // Save food item
        FoodItem savedFoodItem = foodItemRepository.save(newFoodItem);

        // Add item to menu and update menu
        menu.getFoodItems().add(savedFoodItem);
        menuRepository.update(menu);

        // Send success response
        response.status(200);
        sendJson(response, new FoodItemDTO(savedFoodItem));
    }
}