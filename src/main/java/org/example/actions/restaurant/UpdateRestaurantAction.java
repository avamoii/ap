package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.RestaurantDTO;
import org.example.dto.UpdateRestaurantRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

public class UpdateRestaurantAction implements Route {

    private final Gson gson;
    private final RestaurantRepository restaurantRepository;

    public UpdateRestaurantAction(Gson gson, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long ownerIdFromToken = request.attribute("userId");
        UpdateRestaurantRequest updateRequest = gson.fromJson(request.body(), UpdateRestaurantRequest.class);

       //404
        Restaurant restaurantToUpdate = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

      //403
        if (!restaurantToUpdate.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        //409
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().equals(restaurantToUpdate.getPhone())) {
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByPhone(updateRequest.getPhone());
            if (existingRestaurant.isPresent()) {
                throw new ResourceConflictException("This phone number is already in use by another restaurant.");
            }
            restaurantToUpdate.setPhone(updateRequest.getPhone());
        }

        // ۴. سایر فیلدها را در صورت وجود، به‌روزرسانی کن
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

        // ۵. رستوران به‌روز شده را در دیتابیس ذخیره کن
        Restaurant updatedRestaurant = restaurantRepository.update(restaurantToUpdate);

        // ۶. پاسخ موفقیت‌آمیز را به همراه اطلاعات رستوران به‌روز شده، برگردان
        response.status(200);
        return gson.toJson(new RestaurantDTO(updatedRestaurant));
    }
}
