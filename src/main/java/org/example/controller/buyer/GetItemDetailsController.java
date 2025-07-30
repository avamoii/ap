// 4. GET ITEM DETAILS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.FoodItemDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;

import java.util.Optional;

public class GetItemDetailsController extends BaseController {
    private final FoodItemRepository foodItemRepository;

    public GetItemDetailsController(Gson gson, FoodItemRepository foodItemRepository) {
        super(gson);
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long itemId;
        // Parse item ID (400 error handling)
        try {
            itemId = Long.parseLong(request.getPathParam("id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid item ID format. Please provide a numeric ID.");
        }

        // Find the food item by the given ID
        Optional<FoodItem> foodItemOptional = foodItemRepository.findById(itemId);

        // 404 error handling
        FoodItem foodItem = foodItemOptional.orElseThrow(() ->
                new NotFoundException("Food item not found with ID: " + itemId));

        // Send success response
        response.status(200);
        sendJson(response, new FoodItemDTO(foodItem));
    }
}
