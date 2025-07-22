package org.example.dto;

import org.example.enums.CouponType;
import java.math.BigDecimal;

/**
 * DTO for the request body of the POST /admin/coupons endpoint.
 */
public class CreateCouponRequest {
    private String couponCode;
    private CouponType type;
    private BigDecimal value;
    private Integer minPrice;
    private Integer userCount;
    private String startDate; // Received as string in "YYYY-MM-DD" format
    private String endDate;   // Received as string in "YYYY-MM-DD" format

    // Getters and Setters
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
