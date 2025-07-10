package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.FoodItemDTO;
import org.example.dto.UpdateFoodItemRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.repository.FoodItemRepository;
import spark.Request;
import spark.Response;
import spark.Route;

public class UpdateFoodItemAction implements Route {

    private final Gson gson;
    private final FoodItemRepository foodItemRepository;

    public UpdateFoodItemAction(Gson gson, FoodItemRepository foodItemRepository) {
        this.gson = gson;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long itemId = Long.parseLong(request.params(":item_id"));
        Long ownerIdFromToken = request.attribute("userId");
        UpdateFoodItemRequest updateRequest = gson.fromJson(request.body(), UpdateFoodItemRequest.class);

        //404
        FoodItem foodItemToUpdate = foodItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Food item not found."));

        //404
        if (!foodItemToUpdate.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("Food item does not belong to the specified restaurant.");
        }

        //403
        if (!foodItemToUpdate.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // ۴. فیلدها را در صورت وجود، به‌روزرسانی کن
        if (updateRequest.getName() != null) {
            foodItemToUpdate.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            foodItemToUpdate.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            foodItemToUpdate.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getSupply() != null) {
            foodItemToUpdate.setSupply(updateRequest.getSupply());
        }
        if (updateRequest.getImageBase64() != null) {
            foodItemToUpdate.setImageBase64(updateRequest.getImageBase64());
        }
        if (updateRequest.getKeywords() != null) {
            foodItemToUpdate.setKeywords(updateRequest.getKeywords());
        }

        // ۵. آیتم به‌روز شده را در دیتابیس ذخیره کن
        FoodItem updatedFoodItem = foodItemRepository.update(foodItemToUpdate);

        // ۶. پاسخ موفقیت‌آمیز را به همراه اطلاعات آیتم به‌روز شده، برگردان
        response.status(200);
        return gson.toJson(new FoodItemDTO(updatedFoodItem));
    }
}
