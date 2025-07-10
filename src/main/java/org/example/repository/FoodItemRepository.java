package org.example.repository;

import org.example.model.FoodItem;
import java.util.Optional;

/**
 * Interface for data access operations on FoodItem entities.
 */
public interface FoodItemRepository {

    /**
     * Saves a new food item.
     * @param foodItem The food item entity to save.
     * @return The saved food item entity.
     */
    FoodItem save(FoodItem foodItem);

    /**
     * Finds a food item by its unique ID.
     * @param id The ID of the food item to find.
     * @return An Optional containing the food item if found.
     */
    Optional<FoodItem> findById(Long id);

    /**
     * Updates an existing food item.
     * @param foodItem The food item entity with updated information.
     * @return The updated food item entity.
     */
    FoodItem update(FoodItem foodItem);

    /**
     * Deletes a food item from the database.
     * @param foodItem The food item entity to delete.
     */
    void delete(FoodItem foodItem);
}
