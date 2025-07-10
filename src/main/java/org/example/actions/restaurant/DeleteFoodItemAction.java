package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Action to handle the DELETE /restaurants/{id}/item/{item_id} request.
 */
public class DeleteFoodItemAction implements Route {

    private final Gson gson;
    private final FoodItemRepository foodItemRepository;

    public DeleteFoodItemAction(Gson gson, FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long itemId = Long.parseLong(request.params(":item_id"));
        Long ownerIdFromToken = request.attribute("userId");

        //404
        FoodItem foodItemToDelete = foodItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Food item not found."));

       //404
        if (!foodItemToDelete.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("Food item does not belong to the specified restaurant.");
        }

       //403
        if (!foodItemToDelete.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // 4. If all checks pass, delete the food item
        foodItemRepository.delete(foodItemToDelete);

       //200
        response.status(200);
        return gson.toJson(Map.of("message", "Food item removed successfully"));
    }
}
