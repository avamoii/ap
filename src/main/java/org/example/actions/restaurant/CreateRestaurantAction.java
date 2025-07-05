package org.example.actions.restaurant;

import com.google.gson.Gson;
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
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateRestaurantAction implements Route {
    private final Gson gson;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public CreateRestaurantAction(Gson gson, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

      //403
        String userRole = request.attribute("userRole");
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can create restaurants.");
        }

        //400
        CreateRestaurantRequest createRequest = gson.fromJson(request.body(), CreateRestaurantRequest.class);
        if (createRequest.getName() == null || createRequest.getAddress() == null || createRequest.getPhone() == null) {
            throw new InvalidInputException("Missing required fields: name, address, and phone are required.");
        }

        //404
        Long ownerId = request.attribute("userId");
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Seller (owner) not found."));

        // ۴. ساختن موجودیت رستوران جدید
        Restaurant newRestaurant = new Restaurant();
        newRestaurant.setName(createRequest.getName());
        newRestaurant.setAddress(createRequest.getAddress());
        newRestaurant.setPhone(createRequest.getPhone());
        newRestaurant.setLogoBase64(createRequest.getLogoBase64());
        newRestaurant.setTaxFee(createRequest.getTaxFee());
        newRestaurant.setAdditionalFee(createRequest.getAdditionalFee());
        newRestaurant.setOwner(owner); // تنظیم صاحب رستوران

        // ۵. ذخیره رستوران در دیتابیس
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);

        // ۶. آماده‌سازی و ارسال پاسخ موفقیت‌آمیز
        response.status(201); // Created
        return gson.toJson(new RestaurantDTO(savedRestaurant));
    }
}
