package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.ListVendorsRequest;
import org.example.dto.RestaurantDTO;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class ListVendorsAction implements Route {

    private final Gson gson;
    private final RestaurantRepository restaurantRepository;

    public ListVendorsAction(Gson gson, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        ListVendorsRequest filters = new ListVendorsRequest();
        if (request.body() != null && !request.body().isEmpty()) {
            filters = gson.fromJson(request.body(), ListVendorsRequest.class);
        }

        List<Restaurant> vendors = restaurantRepository.findWithFilters(
                filters.getSearch(),
                filters.getKeywords()
        );

        List<RestaurantDTO> vendorDTOs = vendors.stream()
                .map(RestaurantDTO::new)
                .collect(Collectors.toList());

        response.status(200);
        return gson.toJson(vendorDTOs);
    }
}
