package org.example.factory;


import org.example.dto.RegisterRequest;
import org.example.model.*;
        import org.example.model.UserRole;

public class UserFactory {

    public static User createUser(RegisterRequest dto) {
        UserRole role = UserRole.fromString(dto.getRole());

        return switch (role) {
            case CUSTOMER -> new Customer(dto.getFullName(), dto.getPhone(), dto.getEmail(), dto.getAddress());
            case SELLER -> new Seller(dto.getFullName(), dto.getPhone(), dto.getEmail(), dto.getAddress());
            case DELIVERY -> new Delivery(dto.getFullName(), dto.getPhone(), dto.getEmail(), null);
            case ADMIN -> new Admin(dto.getFullName(), dto.getPhone(), dto.getEmail(), dto.getAddress());
        };
    }
}
