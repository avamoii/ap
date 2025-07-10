package org.example.dto;

import org.example.enums.OrderStatus;
import org.example.model.FoodItem;
import org.example.model.Order;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for sending Order information to the client.
 */
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long courierId;
    private List<Long> itemIds;
    private OrderStatus status;
    private Integer payPrice;
    private String createdAt;
    private String updatedAt;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.customerId = order.getCustomer().getId();
        this.restaurantId = order.getRestaurant().getId();
        if (order.getCourier() != null) {
            this.courierId = order.getCourier().getId();
        }
        this.itemIds = order.getItems().stream().map(FoodItem::getId).collect(Collectors.toList());
        this.status = order.getStatus();
        this.payPrice = order.getPayPrice();
        this.createdAt = order.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
        if (order.getUpdatedAt() != null) {
            this.updatedAt = order.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public Long getCourierId() { return courierId; }
    public List<Long> getItemIds() { return itemIds; }
    public OrderStatus getStatus() { return status; }
    public Integer getPayPrice() { return payPrice; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
