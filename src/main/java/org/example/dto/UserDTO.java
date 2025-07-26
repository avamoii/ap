package org.example.dto;

import com.google.gson.annotations.SerializedName;
import org.example.enums.UserRole;
import org.example.model.User;

public class UserDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String address;
    private String email;
    private BankInfoDTO bankInfo;

    // --- فیلد جدید برای موجودی کیف پول ---
    @SerializedName("wallet_balance")
    private Integer walletBalance;

    // --- سازنده جدید برای دریافت تمام اطلاعات ---
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
        this.walletBalance = user.getWalletBalance(); // مقداردهی فیلد جدید
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
}
