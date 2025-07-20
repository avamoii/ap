package org.example.repository;

import org.example.model.Rating;
import java.util.Optional;

public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findByOrderId(Long orderId);
}