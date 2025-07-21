package org.example.enums;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the available payment methods.
 */
public enum PaymentMethod {
    @SerializedName("wallet")
    WALLET,

    @SerializedName("online")
    ONLINE
}
