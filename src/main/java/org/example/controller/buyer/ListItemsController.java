// 3. LIST ITEMS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.FoodItemDTO;
import org.example.dto.ListItemsRequest;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ListItemsController extends BaseController {
    private final FoodItemRepository foodItemRepository;

    public ListItemsController(Gson gson, FoodItemRepository foodItemRepository) {
        super(gson);
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        ListItemsRequest filters = new ListItemsRequest();
        String body = request.getBody();
        if (body != null && !body.isEmpty()) {
            filters = gson.fromJson(body, ListItemsRequest.class);
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
        sendJson(response, itemDTOs);
    }
}