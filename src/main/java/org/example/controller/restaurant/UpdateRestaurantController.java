// 3. UPDATE RESTAURANT CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RestaurantDTO;
import org.example.dto.UpdateRestaurantRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;

import java.util.Optional;

public class UpdateRestaurantController extends BaseController {
    private final RestaurantRepository restaurantRepository;

    public UpdateRestaurantController(Gson gson, RestaurantRepository restaurantRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        Long ownerIdFromToken = getCurrentUserId();
        UpdateRestaurantRequest updateRequest = gson.fromJson(request.getBody(), UpdateRestaurantRequest.class);

        // Find restaurant (404)
        Restaurant restaurantToUpdate = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // Check ownership (403)
        if (!restaurantToUpdate.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Check for phone number conflict (409)
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().equals(restaurantToUpdate.getPhone())) {
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByPhone(updateRequest.getPhone());
            if (existingRestaurant.isPresent()) {
                throw new ResourceConflictException("This phone number is already in use by another restaurant.");
            }
            restaurantToUpdate.setPhone(updateRequest.getPhone());
        }

        // Update other fields if provided
        if (updateRequest.getName() != null) {
            restaurantToUpdate.setName(updateRequest.getName());
        }
        if (updateRequest.getAddress() != null) {
            restaurantToUpdate.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getLogoBase64() != null) {
            restaurantToUpdate.setLogoBase64(updateRequest.getLogoBase64());
        }
        if (updateRequest.getTaxFee() != null) {
            restaurantToUpdate.setTaxFee(updateRequest.getTaxFee());
        }
        if (updateRequest.getAdditionalFee() != null) {
            restaurantToUpdate.setAdditionalFee(updateRequest.getAdditionalFee());
        }

        // Save updated restaurant to database
        Restaurant updatedRestaurant = restaurantRepository.update(restaurantToUpdate);

        // Send success response
        response.status(200);
        sendJson(response, new RestaurantDTO(updatedRestaurant));
    }
}
