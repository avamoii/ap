// 6. DELETE FOOD ITEM CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;

import java.util.Map;

public class DeleteFoodItemController extends BaseController {
    private final FoodItemRepository foodItemRepository;

    public DeleteFoodItemController(Gson gson, FoodItemRepository foodItemRepository) {
        super(gson);
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        Long itemId = Long.parseLong(request.getPathParam("item_id"));
        Long ownerIdFromToken = getCurrentUserId();

        // Find food item (404)
        FoodItem foodItemToDelete = foodItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Food item not found."));

        // Check if item belongs to restaurant (404)
        if (!foodItemToDelete.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("Food item does not belong to the specified restaurant.");
        }

        // Check ownership (403)
        if (!foodItemToDelete.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Delete the food item
        foodItemRepository.delete(foodItemToDelete);

        // Send success response
        response.status(200);
        sendJson(response, Map.of("message", "Food item removed successfully"));
    }
}