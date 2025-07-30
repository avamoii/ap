// 2. GET ITEM RATINGS CONTROLLER
package org.example.controller.buyer;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.ItemRatingsResponseDTO;
import org.example.dto.RatingDTO;
import org.example.model.Rating;
import org.example.repository.RatingRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetItemRatingsController extends BaseController {
    private final RatingRepository ratingRepository;

    public GetItemRatingsController(Gson gson, RatingRepository ratingRepository) {
        super(gson);
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long itemId = Long.parseLong(request.getPathParam("item_id"));

        // Find all ratings for this item
        List<Rating> ratings = ratingRepository.findByFoodItemId(itemId);

        // Calculate average rating
        double avgRating = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0); // If no ratings exist, average is zero

        // Convert list of entities to list of DTOs
        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(RatingDTO::new)
                .collect(Collectors.toList());

        // Build final response object
        ItemRatingsResponseDTO responseDTO = new ItemRatingsResponseDTO(avgRating, ratingDTOs);

        // Send success response
        response.status(200);
        sendJson(response, responseDTO);
    }
}
