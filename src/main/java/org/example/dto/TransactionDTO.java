package org.example.dto;

import com.google.gson.annotations.SerializedName;
import org.example.enums.TransactionType;
import org.example.model.Transaction;
import java.time.format.DateTimeFormatter;

public class TransactionDTO {
    private Long id;
    private Long userId;
    private Long orderId;
    private TransactionType type;
    private Integer amount;
    private String createdAt;

    // --- فیلد جدید برای نمایش نام کاربر در پنل ادمین ---
    @SerializedName("user_name")
    private String userName;

    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.userId = transaction.getUser().getId();
        if (transaction.getOrder() != null) {
            this.orderId = transaction.getOrder().getId();
        }
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.createdAt = transaction.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);

        // --- مقداردهی فیلد جدید ---
        this.userName = (transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName()).trim();
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getOrderId() { return orderId; }
    public TransactionType getType() { return type; }
    public Integer getAmount() { return amount; }
    public String getCreatedAt() { return createdAt; }
    public String getUserName() { return userName; } // Getter برای فیلد جدید
}