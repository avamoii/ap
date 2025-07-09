package org.example.repository;

import org.example.model.FoodItem;
import java.util.Optional;

public interface FoodItemRepository {
    FoodItem save(FoodItem foodItem);

    // متدهای جدید برای ویرایش
    Optional<FoodItem> findById(Long id);
    FoodItem update(FoodItem foodItem);
}
