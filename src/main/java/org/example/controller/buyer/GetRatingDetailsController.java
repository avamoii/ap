// 3. GET RATING DETAILS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RatingDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;

public class GetRatingDetailsController extends BaseController {
    private final RatingRepository ratingRepository;

    public GetRatingDetailsController(Gson gson, RatingRepository ratingRepository) {
        super(gson);
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long ratingId;

        // Validate the input ID from the path parameter (handles 400)
        try {
            ratingId = Long.parseLong(request.getPathParam("id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid rating ID format. Please provide a numeric ID.");
        }

        // Find the rating by the given ID (handles 404)
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found with ID: " + ratingId));

        // If found, convert the entity to a DTO and return it
        response.status(200);
        sendJson(response, new RatingDTO(rating));
    }
}