package org.example.dto;

import org.example.enums.UserRole;

public class RegisterRequest {
    private String fullName;      // <--- تغییر: جایگزین firstName و lastName
    private String phoneNumber;
    private String password;
    private UserRole role;
    private String address;
    private String email;
    private String profileImageBase64;
    private BankInfoDTO bankInfo;

    // Getters and Setters
    public String getFullName() { return fullName; } // <--- تغییر
    public void setFullName(String fullName) { this.fullName = fullName; } // <--- تغییر

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public BankInfoDTO getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfoDTO bankInfo) { this.bankInfo = bankInfo; }
}