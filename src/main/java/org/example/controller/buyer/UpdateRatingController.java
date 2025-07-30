// 4. UPDATE RATING CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RatingDTO;
import org.example.dto.UpdateRatingRequest;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;

public class UpdateRatingController extends BaseController {
    private final RatingRepository ratingRepository;

    public UpdateRatingController(Gson gson, RatingRepository ratingRepository) {
        super(gson);
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long ratingId = Long.parseLong(request.getPathParam("id"));
        Long userIdFromToken = getCurrentUserId();
        UpdateRatingRequest updateRequest = gson.fromJson(request.getBody(), UpdateRatingRequest.class);

        // Find rating (404 error handling)
        Rating ratingToUpdate = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found."));

        // Check ownership (403 error handling)
        if (!ratingToUpdate.getUser().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only update your own ratings.");
        }

        // Input validation (400 error handling)
        if (updateRequest.getRating() != null && (updateRequest.getRating() < 1 || updateRequest.getRating() > 5)) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }

        // Update fields
        if (updateRequest.getRating() != null) {
            ratingToUpdate.setRating(updateRequest.getRating());
        }
        if (updateRequest.getComment() != null) {
            ratingToUpdate.setComment(updateRequest.getComment());
        }
        if (updateRequest.getImageBase64() != null) {
            ratingToUpdate.setImageBase64(updateRequest.getImageBase64());
        }

        // Save changes
        Rating updatedRating = ratingRepository.update(ratingToUpdate);

        // Send success response
        response.status(200);
        sendJson(response, new RatingDTO(updatedRating));
    }
}