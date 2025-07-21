package org.example.dto;

import org.example.enums.CourierOrderStatus;

public class UpdateDeliveryStatusRequest {
    private CourierOrderStatus status;

    public CourierOrderStatus getStatus() {
        return status;
    }

    public void setStatus(CourierOrderStatus status) {
        this.status = status;
    }
}
