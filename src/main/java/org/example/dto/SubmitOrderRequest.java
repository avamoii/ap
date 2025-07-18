package org.example.dto;

import java.util.List;

/**
 * DTO for the request body of the POST /orders endpoint.
 */
public class SubmitOrderRequest {
    private String deliveryAddress;
    private Long vendorId;
    private Long couponId; // Optional
    private List<OrderItemRequestDTO> items;

    // Getters and Setters
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public List<OrderItemRequestDTO> getItems() { return items; }
    public void setItems(List<OrderItemRequestDTO> items) { this.items = items; }
}
