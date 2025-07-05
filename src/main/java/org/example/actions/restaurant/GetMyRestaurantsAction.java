package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.dto.RestaurantDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Restaurant;
import org.example.repository.RestaurantRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class GetMyRestaurantsAction implements Route {

    private final Gson gson;
    private final RestaurantRepository restaurantRepository;

    public GetMyRestaurantsAction(Gson gson, RestaurantRepository restaurantRepository) {
        this.gson = gson;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        // ۱. بررسی نقش کاربر (برای خطای 403)
        String userRole = request.attribute("userRole");
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can view their restaurants.");
        }

        // ۲. دریافت شناسه کاربر از توکن
        Long ownerId = request.attribute("userId");

        // ۳. فراخوانی ریپازیتوری برای دریافت لیست رستوران‌ها
        List<Restaurant> myRestaurants = restaurantRepository.findByOwnerId(ownerId);

        // ۴. تبدیل لیست موجودیت‌های Restaurant به لیست RestaurantDTO
        // این کار تضمین می‌کند که فقط داده‌های امن و مورد نیاز به کلاینت ارسال می‌شود
        List<RestaurantDTO> restaurantDTOS = myRestaurants.stream()
                .map(RestaurantDTO::new) // برای هر رستوران، یک DTO جدید می‌سازد
                .collect(Collectors.toList());

        // ۵. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(restaurantDTOS);
    }
}
