package org.example;

import static spark.Spark.*;
import org.example.model.User;
import java.util.*;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        port(8080);

        get("/hello", (req, res) -> "helloooooooo");

        // In-memory user storage
        List<User> users = new ArrayList<>();
        Gson gson = new Gson();

        // Get all users
        get("/api/v1/users", (req, res) -> {
            res.type("application/json");
            return gson.toJson(users);
        });

        // Get user by id
        get("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if (id < 0 || id >= users.size()) {
                res.status(404);
                return "User not found";
            }
            res.type("application/json");
            return gson.toJson(users.get(id));
        });

        // Create user
        post("/api/v1/users", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            users.add(user);
            res.status(201);
            return gson.toJson(user);
        });

        // Update user by id
        put("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if (id < 0 || id >= users.size()) {
                res.status(404);
                return "User not found";
            }
            User updatedUser = gson.fromJson(req.body(), User.class);
            users.set(id, updatedUser);
            return gson.toJson(updatedUser);
        });

        // Delete user by id
        delete("/api/v1/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if (id < 0 || id >= users.size()) {
                res.status(404);
                return "User not found";
            }
            User removed = users.remove(id);
            return gson.toJson(removed);
        });
    }
}
