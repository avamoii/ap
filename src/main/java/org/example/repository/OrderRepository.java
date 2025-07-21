package org.example.repository;

import org.example.model.Order;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.enums.OrderStatus;

public interface OrderRepository {
    List<Order> findByRestaurantIdWithFilters(Long restaurantId, Map<String, String[]> filters);
    Optional<Order> findById(Long id);
    Order update(Order order);
    Order save(Order order);
    List<Order> findByCustomerIdWithFilters(Long customerId, Map<String, String[]> filters);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCourierIdWithFilters(Long courierId, Map<String, String[]> filters);
}