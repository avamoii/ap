package org.example.dto;

import com.google.gson.annotations.SerializedName;
import org.example.enums.UserRole;
import org.example.enums.UserStatus; // ایمپورت جدید
import org.example.model.User;

public class UserDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String address;
    private String email;
    private BankInfoDTO bankInfo;
    @SerializedName("wallet_balance")
    private Integer walletBalance;

    // --- فیلد جدید برای وضعیت کاربر ---
    private UserStatus status;

    public UserDTO(User user) {
        this.id = user.getId();
        this.fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.address = user.getAddress();
        this.email = user.getEmail();
        if (user.getBankInfo() != null) {
            this.bankInfo = new BankInfoDTO();
            this.bankInfo.setBankName(user.getBankInfo().getBankName());
            this.bankInfo.setAccountNumber(user.getBankInfo().getAccountNumber());
        }
        this.walletBalance = user.getWalletBalance();
        // --- مقداردهی فیلد جدید ---
        this.status = user.getStatus();
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public BankInfoDTO getBankInfo() { return bankInfo; }
    public Integer getWalletBalance() { return walletBalance; }
    // --- Getter برای فیلد جدید ---
    public UserStatus getStatus() { return status; }
}