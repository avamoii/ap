// 1. REGISTER USER CONTROLLER
package org.example.controller.auth;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.RegisterRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.ResourceConflictException;
import org.example.model.BankInfo;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegisterUserController extends BaseController {
    private final UserRepository userRepository;

    public RegisterUserController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Parse request body
        RegisterRequest registerRequest = gson.fromJson(request.getBody(), RegisterRequest.class);

        // Validate required fields
        if (registerRequest.getFullName() == null || registerRequest.getPhone() == null ||
                registerRequest.getPassword() == null || registerRequest.getRole() == null) {
            throw new InvalidInputException("Missing required fields");
        }

        // Check if phone number already exists
        Optional<User> existingUser = userRepository.findByPhoneNumber(registerRequest.getPhone());
        if (existingUser.isPresent()) {
            throw new ResourceConflictException("Phone number already exists");
        }

        // Check if email already exists (if provided)
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            Optional<User> existingEmailUser = userRepository.findByEmail(registerRequest.getEmail());
            if (existingEmailUser.isPresent()) {
                throw new ResourceConflictException("Email already exists");
            }
        }

        // Create new user
        User newUser = new User();

        // Parse full name into first and last name
        String fullName = registerRequest.getFullName();
        String[] names = fullName.trim().split("\\s+", 2);
        String firstName = names.length > 0 ? names[0] : "";
        String lastName = names.length > 1 ? names[1] : "";

        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPhoneNumber(registerRequest.getPhone());
        newUser.setRole(registerRequest.getRole());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setProfileImageBase64(registerRequest.getProfileImageBase64());

        // Set bank info if provided
        if (registerRequest.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(registerRequest.getBankInfo().getBankName());
            bankInfo.setAccountNumber(registerRequest.getBankInfo().getAccountNumber());
            newUser.setBankInfo(bankInfo);
        }

        // Save user to database
        User savedUser = userRepository.save(newUser);

        // Generate JWT token
        String token = JwtUtil.generateToken(savedUser.getId(), savedUser.getRole().toString());

        // Create response data
        UserDTO userDto = new UserDTO(savedUser);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", userDto);
        responseData.put("token", token);

        response.status(200);
        sendJson(response, responseData);
    }
}