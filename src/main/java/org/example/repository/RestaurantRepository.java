package org.example.repository;

import org.example.model.Restaurant;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {

    Restaurant save(Restaurant restaurant);

    List<Restaurant> findByOwnerId(Long ownerId);

    Optional<Restaurant> findById(Long id);

    Optional<Restaurant> findByPhone(String phone);

    Restaurant update(Restaurant restaurant);

    /**
     * Finds restaurants based on optional search and keyword filters.
     * @param search A string to search in the restaurant's name. Can be null.
     * @param keywords A list of keywords to match against food items. Can be null or empty.
     * @return A list of matching restaurants.
     */
    List<Restaurant> findWithFilters(String search, List<String> keywords);
}
