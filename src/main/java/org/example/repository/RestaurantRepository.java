package org.example.repository;

import org.example.model.Restaurant;
import java.util.List;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    List<Restaurant> findByOwnerId(Long ownerId);
}