package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.ItemRatingsResponseDTO;
import org.example.dto.RatingDTO;
import org.example.model.Rating;
import org.example.repository.OrderRepository;
import org.example.repository.RatingRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class GetItemRatingsAction implements Route {

    private final Gson gson;
    private final RatingRepository ratingRepository;

    public GetItemRatingsAction(Gson gson, OrderRepository orderRepository, RatingRepository ratingRepository) {
        this.gson = gson;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long itemId = Long.parseLong(request.params(":item_id"));

        // ۱. پیدا کردن تمام امتیازها برای این آیتم
        List<Rating> ratings = ratingRepository.findByFoodItemId(itemId);

        // ۲. محاسبه میانگین امتیاز
        double avgRating = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0); // اگر هیچ امتیازی وجود نداشت، میانگین صفر است

        // ۳. تبدیل لیست موجودیت‌ها به لیست DTOها
        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(RatingDTO::new)
                .collect(Collectors.toList());

        // ۴. ساختن آبجکت پاسخ نهایی
        ItemRatingsResponseDTO responseDTO = new ItemRatingsResponseDTO(avgRating, ratingDTOs);

        // ۵. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(responseDTO);
    }
}
