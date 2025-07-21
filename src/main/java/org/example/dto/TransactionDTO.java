package org.example.dto;

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

    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.userId = transaction.getUser().getId();
        if (transaction.getOrder() != null) {
            this.orderId = transaction.getOrder().getId();
        }
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.createdAt = transaction.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getOrderId() { return orderId; }
    public TransactionType getType() { return type; }
    public Integer getAmount() { return amount; }
    public String getCreatedAt() { return createdAt; }
}
