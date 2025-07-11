package org.example.dto;

import java.util.List;

/**
 * DTO for the request body of the POST /vendors endpoint.
 * Contains optional filters for searching vendors.
 */
public class ListVendorsRequest {
    private String search;
    private List<String> keywords;

    // Getters and Setters
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
