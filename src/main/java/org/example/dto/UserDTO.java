package org.example.dto;

import org.example.enums.UserRole;

public class UserDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String address;

    public UserDTO(Long id, String lastName, String phoneNumber, String number, UserRole role, String address) {
        this.id = id;
        
        this.fullName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.address = address;
    }

    // Getters (و Setters در صورت نیاز)
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public String getAddress() { return address; }
}