package org.example.repository;

import org.example.model.Order;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters);
    Optional<Order> findById(Long id);
    Order update(Order order);
}