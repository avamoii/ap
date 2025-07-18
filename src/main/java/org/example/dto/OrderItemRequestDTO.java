package org.example.dto;

/**
 * Represents a single item within a larger order request.
 */
public class OrderItemRequestDTO {
    private Long itemId;
    private Integer quantity;

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}