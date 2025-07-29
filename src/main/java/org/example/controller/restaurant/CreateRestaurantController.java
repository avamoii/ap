// 1. CREATE RESTAURANT CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.CreateRestaurantRequest;
import org.example.dto.RestaurantDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Restaurant;
import org.example.model.User;
import org.example.repository.RestaurantRepository;
import org.example.repository.UserRepository;

public class CreateRestaurantController extends BaseController {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public CreateRestaurantController(Gson gson, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        super(gson);
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Check user role (403)
        String userRole = getCurrentUserRole();
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can create restaurants.");
        }

        // Parse and validate request (400)
        CreateRestaurantRequest createRequest = gson.fromJson(request.getBody(), CreateRestaurantRequest.class);
        if (createRequest.getName() == null || createRequest.getAddress() == null || createRequest.getPhone() == null) {
            throw new InvalidInputException("Missing required fields: name, address, and phone are required.");
        }

        // Find owner (404)
        Long ownerId = getCurrentUserId();
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Seller (owner) not found."));

        // Create new restaurant entity
        Restaurant newRestaurant = new Restaurant();
        newRestaurant.setName(createRequest.getName());
        newRestaurant.setAddress(createRequest.getAddress());
        newRestaurant.setPhone(createRequest.getPhone());
        newRestaurant.setLogoBase64(createRequest.getLogoBase64());
        newRestaurant.setTaxFee(createRequest.getTaxFee());
        newRestaurant.setAdditionalFee(createRequest.getAdditionalFee());
        newRestaurant.setOwner(owner);

        // Save restaurant to database
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);

        // Send success response
        response.status(201); // Created
        sendJson(response, new RestaurantDTO(savedRestaurant));
    }
}