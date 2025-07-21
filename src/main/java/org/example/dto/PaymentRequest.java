package org.example.dto;

import org.example.enums.PaymentMethod;

public class PaymentRequest {
    private Long orderId;
    private PaymentMethod method;

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
}
