package org.example.dto;

import org.example.model.FoodItem;
import java.util.List;

public class FoodItemDTO {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer supply;
    private String imageBase64;
    private List<String> keywords;
    private Long restaurantId;

    public FoodItemDTO(FoodItem foodItem) {
        this.id = foodItem.getId();
        this.name = foodItem.getName();
        this.description = foodItem.getDescription();
        this.price = foodItem.getPrice();
        this.supply = foodItem.getSupply();
        this.imageBase64 = foodItem.getImageBase64();
        this.keywords = foodItem.getKeywords();
        if (foodItem.getRestaurant() != null) {
            this.restaurantId = foodItem.getRestaurant().getId();
        }
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Integer getPrice() {
        return price;
    }
    public Integer getSupply() {
        return supply;
    }
    public String getImageBase64() {
        return imageBase64;
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public Long getRestaurantId() {
        return restaurantId;
    }


}