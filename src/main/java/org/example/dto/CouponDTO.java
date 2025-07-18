package org.example.dto;

import org.example.enums.CouponType;
import org.example.model.Coupon;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class CouponDTO {
    private Long id;
    private String couponCode;
    private CouponType type;
    private BigDecimal value;
    private Integer minPrice;
    private Integer userCount;
    private String startDate;
    private String endDate;

    public CouponDTO(Coupon coupon) {
        this.id = coupon.getId();
        this.couponCode = coupon.getCouponCode();
        this.type = coupon.getType();
        this.value = coupon.getValue();
        this.minPrice = coupon.getMinPrice();
        this.userCount = coupon.getUserCount();
        if (coupon.getStartDate() != null) {
            this.startDate = coupon.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (coupon.getEndDate() != null) {
            this.endDate = coupon.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public  String getCouponCode() {
        return  couponCode;
    }
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    public CouponType getType() {
        return  type;
    }
    public void setType(CouponType type) {
        this.type = type;
    }
    public BigDecimal getValue() {
        return   value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    public Integer getMinPrice() {
        return    minPrice;
    }
    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
    public Integer getUserCount() {
        return     userCount;

    }
    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }
    public  String getStartDate() {
        return     startDate;
    }
    public  void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public   String getEndDate() {
        return      endDate;
    }
    public   void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
