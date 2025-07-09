package org.example.repository;

import org.example.model.FoodItem;

/**
 * Interface for data access operations on FoodItem entities.
 */
public interface FoodItemRepository {
    FoodItem save(FoodItem foodItem);
}