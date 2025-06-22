package org.example.dto;

import org.example.enums.UserRole;

public class UserDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String address;
    // می‌توانید فیلدهای email, bankInfo و ... را هم اینجا اضافه کنید اگر می‌خواهید در پاسخ برگردانده شوند

    // ===> Constructor را به این شکل اصلاح کنید <===
    public UserDTO(Long id, String firstName, String lastName, String phoneNumber, UserRole role, String address) {
        this.id = id;
        this.fullName = (firstName + " " + lastName).trim(); // نام و نام خانوادگی را به هم می‌چسباند
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.address = address;
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public String getAddress() { return address; }
}