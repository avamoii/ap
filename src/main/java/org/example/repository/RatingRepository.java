package org.example.repository;

import org.example.model.Rating;
import java.util.*;

public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findByOrderId(Long orderId);
    List<Rating> findByFoodItemId(Long foodItemId);
    Optional<Rating> findById(Long id);
    /**
     * Updates an existing rating.
     * @param rating The rating entity with updated information.
     * @return The updated rating entity.
     */
    Rating update(Rating rating);

    /**
     * Deletes a rating from the database.
     * @param rating The rating entity to delete.
     */
    void delete(Rating rating);
}