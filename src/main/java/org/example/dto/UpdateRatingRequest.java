package org.example.dto;

import java.util.List;

public class UpdateRatingRequest {
    private Integer rating;
    private String comment;
    private List<String> imageBase64;

    // Getters and Setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageBase64() { return imageBase64; }
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }
}
