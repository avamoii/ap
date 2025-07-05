package org.example.repository;

import org.example.model.Restaurant;
import java.util.*;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    List<Restaurant> findByOwnerId(Long ownerId);
    Optional<Restaurant> findById(Long id);
    Optional<Restaurant> findByPhone(String phone);
    Restaurant update(Restaurant restaurant);
}