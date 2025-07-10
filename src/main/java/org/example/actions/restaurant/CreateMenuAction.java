package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.CreateMenuRequest;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Menu;
import org.example.model.Restaurant;
import org.example.repository.MenuRepository;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateMenuAction implements Route {
    private final Gson gson;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;

    public CreateMenuAction(Gson gson, RestaurantRepository restaurantRepository, MenuRepository menuRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        Long ownerIdFromToken = request.attribute("userId");
        String userRole = request.attribute("userRole");

        // ۱. بررسی نقش کاربر (برای خطای 403)
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can create menus.");
        }

        // ۲. خواندن و اعتبارسنجی ورودی (برای خطای 400)
        CreateMenuRequest createRequest = gson.fromJson(request.body(), CreateMenuRequest.class);
        if (createRequest.getTitle() == null || createRequest.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Missing required field: title is required.");
        }

        // ۳. پیدا کردن رستوران (برای خطای 404)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // ۴. بررسی مالکیت (برای خطای 403)
        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // ۵. بررسی تکراری بودن عنوان منو در همین رستوران (برای خطای 409)
        menuRepository.findByRestaurantIdAndTitle(restaurantId, createRequest.getTitle())
                .ifPresent(menu -> {
                    throw new ResourceConflictException("A menu with this title already exists for this restaurant.");
                });

        // ۶. ساختن و ذخیره منوی جدید
        Menu newMenu = new Menu();
        newMenu.setTitle(createRequest.getTitle());
        newMenu.setRestaurant(restaurant);
        Menu savedMenu = menuRepository.save(newMenu);

        // ۷. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(Map.of("title", savedMenu.getTitle()));
    }
}
