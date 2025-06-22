package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable // <-- این Annotation به هایبرنیت می‌گوید که این کلاس می‌تواند در یک Entity دیگر جاسازی شود
public class BankInfo {

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    // Getters and Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}