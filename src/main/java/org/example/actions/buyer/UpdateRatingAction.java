package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.RatingDTO;
import org.example.dto.UpdateRatingRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;
import spark.Request;
import spark.Response;
import spark.Route;

public class UpdateRatingAction implements Route {
    private final Gson gson;
    private final RatingRepository ratingRepository;

    public UpdateRatingAction(Gson gson, RatingRepository ratingRepository) {
        this.gson = gson;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long ratingId = Long.parseLong(request.params(":id"));
        Long userIdFromToken = request.attribute("userId");
        UpdateRatingRequest updateRequest = gson.fromJson(request.body(), UpdateRatingRequest.class);

        // ۱. پیدا کردن امتیاز (برای خطای 404)
        Rating ratingToUpdate = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found."));

        // ۲. بررسی مالکیت (برای خطای 403)
        if (!ratingToUpdate.getUser().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only update your own ratings.");
        }

        // ۳. اعتبارسنجی ورودی (برای خطای 400)
        if (updateRequest.getRating() != null && (updateRequest.getRating() < 1 || updateRequest.getRating() > 5)) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }

        // ۴. به‌روزرسانی فیلدها
        if (updateRequest.getRating() != null) {
            ratingToUpdate.setRating(updateRequest.getRating());
        }
        if (updateRequest.getComment() != null) {
            ratingToUpdate.setComment(updateRequest.getComment());
        }
        if (updateRequest.getImageBase64() != null) {
            ratingToUpdate.setImageBase64(updateRequest.getImageBase64());
        }

        // ۵. ذخیره تغییرات
        Rating updatedRating = ratingRepository.update(ratingToUpdate);

        // ۶. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(new RatingDTO(updatedRating));
    }
}
