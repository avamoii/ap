package org.example.controller;


import com.google.gson.Gson;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.factory.UserFactory;
import io.jsonwebtoken.Claims;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.service.UserService;
import org.example.util.JwtUtil;

import java.util.Map;

import static spark.Spark.*;

public class UserController {

    private static final Gson gson = new Gson();
    private static final UserService userService = new UserService();

    public static void initRoutes() {

        path("/api/users", () -> {


            post("/register", (req, res) -> {
                try {
                    RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
                    userService.register(request);
                    res.status(201);
                    return gson.toJson(Map.of("message", "User created successfully"));
                } catch (IllegalArgumentException e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", e.getMessage()));
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Internal server error"));
                }
            });
        });
    }
}



