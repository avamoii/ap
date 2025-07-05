package org.example.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String profileImageBase64;
    private BankInfoDTO bankInfo;

    // Getters and Setters for all fields
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public BankInfoDTO getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfoDTO bankInfo) { this.bankInfo = bankInfo; }
}
