package org.example.service;

import java.util.*;
import com.google.gson.Gson;
import org.example.model.User;

public class UserServices {
    // In-memory user storage
    private List<User> users = new ArrayList<>();
    private Gson gson = new Gson();

    // Get all users
    public String getAllUsers() {
        return gson.toJson(users);
    }

    // Get user by id
    public String getUserById(int id) {
        if (id < 0 || id >= users.size()) {
            return "User not found";
        }
        return gson.toJson(users.get(id));
    }

    // Create user
    public String createUser(String userJson) {
        User user = gson.fromJson(userJson, User.class);
        users.add(user);
        return gson.toJson(user);
    }

    // Update user by id
    public String updateUserById(int id, String userJson) {
        if (id < 0 || id >= users.size()) {
            return "User not found";
        }
        User updatedUser = gson.fromJson(userJson, User.class);
        users.set(id, updatedUser);
        return gson.toJson(updatedUser);
    }

    // Delete user by id
    public String deleteUserById(int id) {
        if (id < 0 || id >= users.size()) {
            return "User not found";
        }
        users.remove(id);
        return "User deleted";
    }
}
