package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.FoodItemDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

/**
 * Action to handle the GET /items/{id} request.
 * Fetches the details of a specific food item.
 */
public class GetItemDetailsAction implements Route {

    private final Gson gson;
    private final FoodItemRepository foodItemRepository;

    public GetItemDetailsAction(Gson gson, FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        Long itemId;
       //400
        try {
            itemId = Long.parseLong(request.params(":id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid item ID format. Please provide a numeric ID.");
        }

        // 2. Find the food item by the given ID.
        Optional<FoodItem> foodItemOptional = foodItemRepository.findById(itemId);

        //404
        FoodItem foodItem = foodItemOptional.orElseThrow(() -> new NotFoundException("Food item not found with ID: " + itemId));

        //200
        response.status(200);
        return gson.toJson(new FoodItemDTO(foodItem));
    }
}
