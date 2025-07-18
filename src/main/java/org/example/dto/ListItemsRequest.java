package org.example.dto;

import java.util.List;

/**
 * DTO for the request body of the POST /items endpoint.
 * Contains optional filters for searching food items.
 */
public class ListItemsRequest {
    private String search;
    private Integer price; // A filter for items with a price less than or equal to this value
    private List<String> keywords;

    // Getters and Setters
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
