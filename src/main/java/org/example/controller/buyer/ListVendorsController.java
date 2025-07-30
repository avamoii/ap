// 1. LIST VENDORS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.ListVendorsRequest;
import org.example.dto.RestaurantDTO;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ListVendorsController extends BaseController {
    private final RestaurantRepository restaurantRepository;

    public ListVendorsController(Gson gson, RestaurantRepository restaurantRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        ListVendorsRequest filters = new ListVendorsRequest();
        String body = request.getBody();
        if (body != null && !body.isEmpty()) {
            filters = gson.fromJson(body, ListVendorsRequest.class);
        }

        List<Restaurant> vendors = restaurantRepository.findWithFilters(
                filters.getSearch(),
                filters.getKeywords()
        );

        List<RestaurantDTO> vendorDTOs = vendors.stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        sendJson(response, vendorDTOs);
    }
}