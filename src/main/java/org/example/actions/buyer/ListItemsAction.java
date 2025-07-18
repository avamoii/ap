package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.FoodItemDTO;
import org.example.dto.ListItemsRequest;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class ListItemsAction implements Route {

    private final Gson gson;
    private final FoodItemRepository foodItemRepository;

    public ListItemsAction(Gson gson, FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        ListItemsRequest filters = new ListItemsRequest();
        if (request.body() != null && !request.body().isEmpty()) {
            filters = gson.fromJson(request.body(), ListItemsRequest.class);
        }

        List<FoodItem> items = foodItemRepository.findWithFilters(
                filters.getSearch(),
                filters.getPrice(),
                filters.getKeywords()
        );

        List<FoodItemDTO> itemDTOs = items.stream()
                .map(FoodItemDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        return gson.toJson(itemDTOs);
    }
}
