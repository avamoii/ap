package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.RatingDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Rating;
import org.example.repository.RatingRepository;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Action to handle the GET /ratings/{id} request.
 * Fetches the details of a specific rating.
 */
public class GetRatingDetailsAction implements Route {

    private final Gson gson;
    private final RatingRepository ratingRepository;

    public GetRatingDetailsAction(Gson gson, RatingRepository ratingRepository) {
        this.gson = gson;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        Long ratingId;
        // 1. Validate the input ID from the path parameter (handles 400)
        try {
            ratingId = Long.parseLong(request.params(":id"));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid rating ID format. Please provide a numeric ID.");
        }

        // 2. Find the rating by the given ID (handles 404)
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found with ID: " + ratingId));

        // 3. If found, convert the entity to a DTO and return it.
        response.status(200);
        return gson.toJson(new RatingDTO(rating));
    }
}
