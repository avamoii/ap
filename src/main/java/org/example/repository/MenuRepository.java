package org.example.repository;

import org.example.model.Menu;
import java.util.Optional;

/**
 * Interface for data access operations on Menu entities.
 */
public interface MenuRepository {

    /**
     * Saves a new menu.
     * @param menu The menu entity to save.
     * @return The saved menu entity.
     */
    Menu save(Menu menu);

    /**
     * Finds a menu by its title within a specific restaurant.
     * @param restaurantId The ID of the restaurant.
     * @param title The title of the menu.
     * @return An Optional containing the menu if found.
     */
    Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title);

    /**
     * Updates an existing menu.
     * @param menu The menu entity with updated information.
     * @return The updated menu entity.
     */
    Menu update(Menu menu);
}
