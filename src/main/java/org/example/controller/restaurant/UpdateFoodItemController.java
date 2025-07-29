// 5. UPDATE FOOD ITEM CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.FoodItemDTO;
import org.example.dto.UpdateFoodItemRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;

public class UpdateFoodItemController extends BaseController {
    private final FoodItemRepository foodItemRepository;

    public UpdateFoodItemController(Gson gson, FoodItemRepository foodItemRepository) {
        super(gson);
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        Long itemId = Long.parseLong(request.getPathParam("item_id"));
        Long ownerIdFromToken = getCurrentUserId();
        UpdateFoodItemRequest updateRequest = gson.fromJson(request.getBody(), UpdateFoodItemRequest.class);

        // Find food item (404)
        FoodItem foodItemToUpdate = foodItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Food item not found."));

        // Check if item belongs to restaurant (404)
        if (!foodItemToUpdate.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("Food item does not belong to the specified restaurant.");
        }

        // Check ownership (403)
        if (!foodItemToUpdate.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Update fields if provided
        if (updateRequest.getName() != null) {
            foodItemToUpdate.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            foodItemToUpdate.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            foodItemToUpdate.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getSupply() != null) {
            foodItemToUpdate.setSupply(updateRequest.getSupply());
        }
        if (updateRequest.getImageBase64() != null) {
            foodItemToUpdate.setImageBase64(updateRequest.getImageBase64());
        }
        if (updateRequest.getKeywords() != null) {
            foodItemToUpdate.setKeywords(updateRequest.getKeywords());
        }

        // Save updated item
        FoodItem updatedFoodItem = foodItemRepository.update(foodItemToUpdate);

        // Send success response
        response.status(200);
        sendJson(response, new FoodItemDTO(updatedFoodItem));
    }
}