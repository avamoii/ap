package org.example.controller;

import com.google.gson.Gson;
import org.example.dto.RegisterRequest;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.JwtUtil; // استفاده از JwtUtil جدید
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationController {

    private final UserService userService;
    private final Gson gson;

    public AuthenticationController(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    public String handleRegistration(Request request, Response response) {
        response.type("application/json");
        try {
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

            if (registerRequest.getPhoneNumber() == null || registerRequest.getPassword() == null ||
                    registerRequest.getFirstName() == null || registerRequest.getLastName() == null ||
                    registerRequest.getRole() == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Missing required fields"));
            }

            User registeredUser = userService.registerUser(registerRequest);
            // تغییر در این خط: به جای پاس دادن کل آبجکت User،
            // ID و Role را از آن استخراج کرده و به عنوان دو آرگومان جداگانه پاس دهید.
            String token = JwtUtil.generateToken(registeredUser.getId(), registeredUser.getRole().toString());

            response.status(201);
            UserDTO userDto = new UserDTO(
                    registeredUser.getId(),
                    registeredUser.getFirstName(),
                    registeredUser.getLastName(),
                    registeredUser.getPhoneNumber(),
                    registeredUser.getRole(),
                    registeredUser.getAddress()
            );
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", userDto);
            responseData.put("token", token);
            return gson.toJson(responseData);

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Phone number already exists")) {
                response.status(409); // Conflict
                return gson.toJson(Map.of("error", e.getMessage()));
            }
            response.status(500); // Internal Server Error
            e.printStackTrace();
            return gson.toJson(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    // متد لاگین (برای پیاده‌سازی در آینده)
    // public String handleLogin(Request request, Response response) { ... }
}