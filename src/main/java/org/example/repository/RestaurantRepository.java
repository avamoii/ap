package org.example.repository;

import org.example.model.Restaurant;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
}