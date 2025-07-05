package org.example.dto;

import org.example.model.Restaurant;

// This DTO is used to send restaurant data back to the client.
public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private Integer taxFee;
    private Integer additionalFee;

    // Constructor to easily convert an Entity to a DTO
    public RestaurantDTO(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.phone = restaurant.getPhone();
        this.logoBase64 = restaurant.getLogoBase64();
        this.taxFee = restaurant.getTaxFee();
        this.additionalFee = restaurant.getAdditionalFee();
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getLogoBase64() { return logoBase64; }
    public Integer getTaxFee() { return taxFee; }
    public Integer getAdditionalFee() { return additionalFee; }
}
