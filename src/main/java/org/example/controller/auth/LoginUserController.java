// 2. LOGIN USER CONTROLLER
package org.example.controller.auth;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.LoginRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.UnauthorizedException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginUserController extends BaseController {
    private final UserRepository userRepository;

    public LoginUserController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Parse request body
        LoginRequest loginRequest = gson.fromJson(request.getBody(), LoginRequest.class);

        // Validate phone field
        if (loginRequest.getPhone() == null || loginRequest.getPhone().trim().isEmpty()) {
            throw new InvalidInputException("Invalid `phone`");
        }

        // Validate password field
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new InvalidInputException("Invalid `password`");
        }

        // Find user by phone number
        Optional<User> userOptional = userRepository.findByPhoneNumber(loginRequest.getPhone());
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User with this phone number not found");
        }

        User user = userOptional.get();

        // Verify password
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        // Generate JWT token
        String token = JwtUtil.generateToken(user.getId(), user.getRole().toString());

        // Create response data
        UserDTO userDto = new UserDTO(user);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "User logged in successfully");
        responseData.put("token", token);
        responseData.put("user", userDto);

        response.status(200);
        sendJson(response, responseData);
    }
}