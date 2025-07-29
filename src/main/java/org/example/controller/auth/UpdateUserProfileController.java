// 4. UPDATE USER PROFILE CONTROLLER
package org.example.controller.auth;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.UpdateProfileRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.BankInfo;
import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

public class UpdateUserProfileController extends BaseController {
    private final UserRepository userRepository;

    public UpdateUserProfileController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Get user ID from JWT context
        Long userId = getCurrentUserId();

        // Parse request body
        UpdateProfileRequest updateRequest = gson.fromJson(request.getBody(), UpdateProfileRequest.class);

        // Find current user (404 if not found)
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Resource not found."));

        // Check if phone number is being changed and if it's already taken (409)
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().equals(currentUser.getPhoneNumber())) {
            Optional<User> userWithSamePhone = userRepository.findByPhoneNumber(updateRequest.getPhone());
            if (userWithSamePhone.isPresent()) {
                throw new ResourceConflictException("This phone number is already taken by another user.");
            }
            currentUser.setPhoneNumber(updateRequest.getPhone());
        }

        // Check if email is being changed and if it's already taken (409)
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(currentUser.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(updateRequest.getEmail());
            if (userWithSameEmail.isPresent()) {
                throw new ResourceConflictException("This email is already taken by another user.");
            }
            currentUser.setEmail(updateRequest.getEmail());
        }

        // Update full name if provided
        if (updateRequest.getFullName() != null) {
            String[] names = updateRequest.getFullName().trim().split("\\s+", 2);
            currentUser.setFirstName(names.length > 0 ? names[0] : "");
            currentUser.setLastName(names.length > 1 ? names[1] : "");
        }

        // Update address if provided
        if (updateRequest.getAddress() != null) {
            currentUser.setAddress(updateRequest.getAddress());
        }

        // Update profile image if provided
        if (updateRequest.getProfileImageBase64() != null) {
            currentUser.setProfileImageBase64(updateRequest.getProfileImageBase64());
        }

        // Update bank info if provided
        if (updateRequest.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(updateRequest.getBankInfo().getBankName());
            bankInfo.setAccountNumber(updateRequest.getBankInfo().getAccountNumber());
            currentUser.setBankInfo(bankInfo);
        }

        // Save updated user to database
        userRepository.update(currentUser);

        // Return success response
        response.status(200);
        sendJson(response, Map.of("message", "Profile updated successfully"));
    }
}