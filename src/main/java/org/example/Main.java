package org.example;

import static spark.Spark.*;
import org.example.model.User;
import java.util.*;
import com.google.gson.Gson;
import org.example.service.UserServices;

public class Main {
    public static void main(String[] args) {
        port(8080);

        get("/hello", (req, res) -> "helloooooooo");

        UserServices userServices = new UserServices();

        // Get all users
        get("/api/v1/users", (req, res) -> {
            res.type("application/json");
            return userServices.getAllUsers();
        });

        // Get user by id
        get("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            String result = userServices.getUserById(id);
            if ("User not found".equals(result)) {
                res.status(404);
            } else {
                res.type("application/json");
            }
            return result;
        });

        // Create user
        post("/api/v1/users", (req, res) -> {
            String result = userServices.createUser(req.body());
            if ("Failed to create user".equals(result)) {
                res.status(400);
            } else {
                res.status(201);
                res.type("application/json");
            }
            return result;
        });

        // Update user by id
        put("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            String result = userServices.updateUserById(id, req.body());
            if ("User not found or update failed".equals(result)) {
                res.status(404);
            } else {
                res.type("application/json");
            }
            return result;
        });

        // Delete user by id
        delete("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            String result = userServices.deleteUserById(id);
            if ("User not found or delete failed".equals(result)) {
                res.status(404);
            }
            return result;
        });
    }
}
