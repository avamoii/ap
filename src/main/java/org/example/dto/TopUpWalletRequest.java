package org.example.dto;

/**
 * DTO for the request body of the POST /wallet/top-up endpoint.
 */
public class TopUpWalletRequest {
    private Integer amount;

    // Getter and Setter
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
