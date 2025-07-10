package org.example.repository;

import org.example.model.Order;
import java.util.List;
import java.util.Map;

public interface OrderRepository {
    List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters);
}