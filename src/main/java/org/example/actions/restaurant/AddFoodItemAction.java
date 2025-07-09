package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.AddFoodItemRequest;
import org.example.dto.FoodItemDTO;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.model.Restaurant;
import org.example.repository.FoodItemRepository;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddFoodItemAction implements Route {
    private final Gson gson;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    public AddFoodItemAction(Gson gson, RestaurantRepository restaurantRepository, FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long ownerIdFromToken = request.attribute("userId");
        AddFoodItemRequest addRequest = gson.fromJson(request.body(), AddFoodItemRequest.class);

        // ۱. اعتبارسنجی ورودی (برای خطای 400)
        if (addRequest.getName() == null || addRequest.getDescription() == null || addRequest.getPrice() == null || addRequest.getSupply() == null || addRequest.getKeywords() == null || addRequest.getKeywords().isEmpty()) {
            throw new InvalidInputException("Missing required fields: name, description, price, supply, and keywords are required.");
        }

        // ۲. پیدا کردن رستوران (برای خطای 404)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // ۳. بررسی مالکیت (برای خطای 403)
        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // ۴. ساختن موجودیت آیتم غذایی جدید
        FoodItem newFoodItem = new FoodItem();
        newFoodItem.setName(addRequest.getName());
        newFoodItem.setDescription(addRequest.getDescription());
        newFoodItem.setPrice(addRequest.getPrice());
        newFoodItem.setSupply(addRequest.getSupply());
        newFoodItem.setImageBase64(addRequest.getImageBase64());
        newFoodItem.setKeywords(addRequest.getKeywords());
        newFoodItem.setRestaurant(restaurant); // تنظیم ارتباط با رستوران

        // ۵. ذخیره آیتم غذایی در دیتابیس
        FoodItem savedFoodItem = foodItemRepository.save(newFoodItem);

        // ۶. آماده‌سازی و ارسال پاسخ موفقیت‌آمیز
        response.status(200); // طبق API (201 صحیح‌تر است)
        return gson.toJson(new FoodItemDTO(savedFoodItem));
    }
}
