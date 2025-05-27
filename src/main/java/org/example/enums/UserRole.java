package org.example.enums;

public enum UserRole {
    CUSTOMER, SELLER, DELIVERY,ADMIN;

    public static UserRole fromString(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> ADMIN;
            case "buyer", "customer" -> CUSTOMER;
            case "vendor", "seller" -> SELLER;
            case "deliveryagent", "delivery" -> DELIVERY;
            default -> throw new IllegalArgumentException("Invalid user role: " + role);
        };
    }
}



