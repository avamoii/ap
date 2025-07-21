package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.UpdateUserStatusRequest;
import org.example.enums.UserRole;
import org.example.enums.UserStatus;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Action to handle the PATCH /admin/users/{id}/status request.
 * Updates the approval status of a user, accessible only by an admin.
 */
public class UpdateUserStatusAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public UpdateUserStatusAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");
        Long targetUserId = Long.parseLong(request.params(":id"));
        UpdateUserStatusRequest statusRequest = gson.fromJson(request.body(), UpdateUserStatusRequest.class);

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Validate the input status (handles 400 Invalid Input)
        if (statusRequest.getStatus() == null ||
                (!statusRequest.getStatus().equalsIgnoreCase("approved") && !statusRequest.getStatus().equalsIgnoreCase("rejected"))) {
            throw new InvalidInputException("Invalid status. Must be 'approved' or 'rejected'.");
        }

        // 3. Find the target user (handles 404 Not Found)
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User with ID " + targetUserId + " not found."));

        // 4. Update the user's status
        UserStatus newStatus = statusRequest.getStatus().equalsIgnoreCase("approved") ?
                UserStatus.APPROVED : UserStatus.REJECTED;
        targetUser.setStatus(newStatus);

        // 5. Save the updated user to the database
        userRepository.update(targetUser);

        // 6. Send the successful response
        response.status(200);
        return ""; // The API specifies no response body on success
    }
}
