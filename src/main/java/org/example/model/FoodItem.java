package org.example.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1024) // A longer column for description
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer supply;

    @Lob // For large objects, suitable for Base64 strings
    private String imageBase64;

    // Using ElementCollection for a simple list of strings (keywords)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_item_keywords", joinColumns = @JoinColumn(name = "food_item_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    // A food item belongs to one restaurant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    // A food item can be in many menus, and a menu can have many food items.
    @ManyToMany(mappedBy = "foodItems")
    private List<Menu> menus = new ArrayList<>();
    // Getters and Setters
    public List<Menu> getMenus() { return menus; }
    public void setMenus(List<Menu> menus) { this.menus = menus; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
}
