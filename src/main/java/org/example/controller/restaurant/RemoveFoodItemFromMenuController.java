// 8. REMOVE FOOD ITEM FROM MENU CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Menu;
import org.example.repository.MenuRepository;

import java.util.Map;

public class RemoveFoodItemFromMenuController extends BaseController {
    private final MenuRepository menuRepository;

    public RemoveFoodItemFromMenuController(Gson gson, MenuRepository menuRepository) {
        super(gson);
        this.menuRepository = menuRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        String menuTitle = request.getPathParam("title");
        Long itemIdToRemove = Long.parseLong(request.getPathParam("item_id"));
        Long ownerIdFromToken = getCurrentUserId();

        // Find menu (404)
        Menu menu = menuRepository.findByRestaurantIdAndTitle(restaurantId, menuTitle)
                .orElseThrow(() -> new NotFoundException("Menu with title '" + menuTitle + "' not found in this restaurant."));

        // Check ownership (403)
        if (!menu.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Remove the food item from the menu's list
        boolean removed = menu.getFoodItems().removeIf(foodItem -> foodItem.getId().equals(itemIdToRemove));

        // Check if item was found in menu (404)
        if (!removed) {
            throw new NotFoundException("Food item with ID " + itemIdToRemove + " was not found in this menu.");
        }

        // Update menu in database
        menuRepository.update(menu);

        // Send success response
        response.status(200);
        sendJson(response, Map.of("message", "Item removed from restaurant menu successfully"));
    }
}