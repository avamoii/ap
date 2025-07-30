// 5. DELETE RATING CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;

import java.util.Map;

public class DeleteRatingController extends BaseController {
    private final RatingRepository ratingRepository;

    public DeleteRatingController(Gson gson, RatingRepository ratingRepository) {
        super(gson);
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long ratingId = Long.parseLong(request.getPathParam("id"));
        Long userIdFromToken = getCurrentUserId();

        // Find rating (404 error handling)
        Rating ratingToDelete = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found."));

        // Check ownership (403 error handling)
        if (!ratingToDelete.getUser().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only delete your own ratings.");
        }

        // Delete rating
        ratingRepository.delete(ratingToDelete);

        // Send success response
        response.status(200);
        sendJson(response, Map.of("message", "Rating deleted"));
    }
}