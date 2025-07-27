package org.example.dto;

import com.google.gson.annotations.SerializedName;
import org.example.enums.OrderStatus;
import org.example.model.FoodItem;
import org.example.model.Order;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDTO {
    private Long id;
    private String deliveryAddress;
    private Long customerId;
    private Long restaurantId;
    private Long courierId;
    private Long couponId;
    private List<Long> itemIds;
    private Integer rawPrice;
    private Integer taxFee;
    private Integer additionalFee;
    private Integer courierFee;
    private Integer payPrice;
    private OrderStatus status;
    private String createdAt;
    private String updatedAt;

    // --- فیلد جدید برای شناسه نظر ---
    @SerializedName("rating_id")
    private Long ratingId;


    public OrderDTO(Order order) {
        this.id = order.getId();
        this.deliveryAddress = order.getDeliveryAddress();
        this.customerId = order.getCustomer().getId();
        this.restaurantId = order.getRestaurant().getId();
        if (order.getCourier() != null) {
            this.courierId = order.getCourier().getId();
        }
        if (order.getCoupon() != null) {
            this.couponId = order.getCoupon().getId();
        }
        this.itemIds = order.getItems().stream().map(FoodItem::getId).collect(Collectors.toList());
        this.rawPrice = order.getRawPrice();
        this.taxFee = order.getTaxFee();
        this.additionalFee = order.getAdditionalFee();
        this.courierFee = order.getCourierFee();
        this.payPrice = order.getPayPrice();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
        if (order.getUpdatedAt() != null) {
            this.updatedAt = order.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        // --- مقداردهی فیلد جدید ---
        //  (برای این کار باید یک رابطه یک به یک بین Order و Rating ایجاد کنیم)
        // if (order.getRating() != null) {
        //     this.ratingId = order.getRating().getId();
        // }
    }

    // Getters
    public Long getId() { return id; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Long getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public Long getCourierId() { return courierId; }
    public Long getCouponId() { return couponId; }
    public List<Long> getItemIds() { return itemIds; }
    public Integer getRawPrice() { return rawPrice; }
    public Integer getTaxFee() { return taxFee; }
    public Integer getAdditionalFee() { return additionalFee; }
    public Integer getCourierFee() { return courierFee; }
    public Integer getPayPrice() { return payPrice; }
    public OrderStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Getter برای فیلد جدید
    public Long getRatingId() { return ratingId; }
}