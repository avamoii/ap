package org.example.dto;

import org.example.enums.VendorOrderStatus;

public class UpdateOrderStatusRequest {
    private VendorOrderStatus status;

    public VendorOrderStatus getStatus() {
        return status;
    }

    public void setStatus(VendorOrderStatus status) {
        this.status = status;
    }
}
