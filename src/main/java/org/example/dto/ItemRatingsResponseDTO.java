package org.example.dto;

import java.util.List;

public class ItemRatingsResponseDTO {
    private Double avgRating;
    private List<RatingDTO> comments;

    public ItemRatingsResponseDTO(Double avgRating, List<RatingDTO> comments) {
        this.avgRating = avgRating;
        this.comments = comments;
    }
    public Double getAvgRating() {
        return avgRating;
    }
    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }
    public List<RatingDTO> getComments() {
        return comments;
    }
    public void setComments(List<RatingDTO> comments) {
        this.comments = comments;
    }


}