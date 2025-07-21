package org.example.dto;

/**
 * DTO for the request body of the PATCH /admin/users/{id}/status endpoint.
 */
public class UpdateUserStatusRequest {
    private String status; // We receive it as a string ("approved" or "rejected")

    // Getter and Setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
