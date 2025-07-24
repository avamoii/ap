package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.AddFoodItemRequest;
import org.example.dto.FoodItemDTO;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.FoodItem;
import org.example.model.Menu;
import org.example.model.Restaurant;
import org.example.repository.FoodItemRepository;
import org.example.repository.MenuRepository;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddFoodItemAction implements Route {
    private final Gson gson;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final MenuRepository menuRepository; // <-- ریپازیتوری جدید

    public AddFoodItemAction(Gson gson, RestaurantRepository restaurantRepository, FoodItemRepository foodItemRepository, MenuRepository menuRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        String menuTitle = request.params(":title"); // <-- خواندن عنوان منو از URL
        Long ownerIdFromToken = request.attribute("userId");
        AddFoodItemRequest addRequest = gson.fromJson(request.body(), AddFoodItemRequest.class);

        if (addRequest.getName() == null || addRequest.getDescription() == null || addRequest.getPrice() == null || addRequest.getSupply() == null || addRequest.getKeywords() == null || addRequest.getKeywords().isEmpty()) {
            throw new InvalidInputException("Missing required fields.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied.");
        }

        // **مرحله کلیدی ۱: پیدا کردن منوی صحیح**
        Menu menu = menuRepository.findByRestaurantIdAndTitle(restaurantId, menuTitle)
                .orElseThrow(() -> new NotFoundException("Menu with title '" + menuTitle + "' not found."));

        FoodItem newFoodItem = new FoodItem();
        newFoodItem.setName(addRequest.getName());
        newFoodItem.setDescription(addRequest.getDescription());
        newFoodItem.setPrice(addRequest.getPrice());
        newFoodItem.setSupply(addRequest.getSupply());
        newFoodItem.setImageBase64(addRequest.getImageBase64());
        newFoodItem.setKeywords(addRequest.getKeywords());
        newFoodItem.setRestaurant(restaurant);

        FoodItem savedFoodItem = foodItemRepository.save(newFoodItem);

        // **مرحله کلیدی ۲: اتصال آیتم به لیست آیتم‌های منو و آپدیت منو**
        menu.getFoodItems().add(savedFoodItem);
        menuRepository.update(menu);

        response.status(200);
        return gson.toJson(new FoodItemDTO(savedFoodItem));
    }
}