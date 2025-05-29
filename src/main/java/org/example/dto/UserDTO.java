package org.example.dto;

import org.example.enums.UserRole;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private String address;

    public UserDTO(Long id, String firstName, String lastName, String phoneNumber, UserRole role, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.address = address;
    }

    // Getters (و Setters در صورت نیاز)
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public String getAddress() { return address; }
}