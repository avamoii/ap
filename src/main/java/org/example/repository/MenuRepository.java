package org.example.repository;

import org.example.model.Menu;
import java.util.Optional;

public interface MenuRepository {
    Menu save(Menu menu);
    Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title);
}