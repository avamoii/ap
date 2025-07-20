package org.example.dto;

import org.example.model.Rating;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RatingDTO {
    private Long id;
    private Long itemId;
    private Integer rating;
    private String comment;
    private List<String> imageBase64;
    private Long userId;
    private String createdAt;

    public RatingDTO(Rating rating) {
        this.id = rating.getId();
        this.itemId = rating.getFoodItem().getId();
        this.rating = rating.getRating();
        this.comment = rating.getComment();
        this.imageBase64 = rating.getImageBase64();
        this.userId = rating.getUser().getId();
        this.createdAt = rating.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getItemId() {
        return itemId;
    }
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public List<String> getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(List<String> imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}