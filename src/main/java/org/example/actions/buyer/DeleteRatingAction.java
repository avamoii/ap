package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class DeleteRatingAction implements Route {
    private final Gson gson;
    private final RatingRepository ratingRepository;

    public DeleteRatingAction(Gson gson, RatingRepository ratingRepository) {
        this.gson = gson;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long ratingId = Long.parseLong(request.params(":id"));
        Long userIdFromToken = request.attribute("userId");

        // ۱. پیدا کردن امتیاز (برای خطای 404)
        Rating ratingToDelete = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found."));

        // ۲. بررسی مالکیت (برای خطای 403)
        if (!ratingToDelete.getUser().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only delete your own ratings.");
        }

        // ۳. حذف امتیاز
        ratingRepository.delete(ratingToDelete);

        // ۴. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(Map.of("message", "Rating deleted"));
    }
}
