package org.example.service;

import java.util.*;
import com.google.gson.Gson;
import org.example.model.User;
import org.example.DAO.UserDao;

public class UserServices {
    private UserDao userDao = UserDao.getInstance();
    private Gson gson = new Gson();

    public String getAllUsers() {
        List<User> users = userDao.getAllUsers();
        return gson.toJson(users);
    }

    public String getUserById(int id) {
        User user = userDao.getUserById(id);
        if (user == null) {
            return "User not found";
        }
        return gson.toJson(user);
    }

    public String createUser(String userJson) {
        User user = gson.fromJson(userJson, User.class);
        boolean success = userDao.createUser(user);
        if (success) {
            return gson.toJson(user);
        } else {
            return "Failed to create user";
        }
    }

    public String updateUserById(int id, String userJson) {
        User updatedUser = gson.fromJson(userJson, User.class);
        boolean success = userDao.updateUser(id, updatedUser);
        if (success) {
            return gson.toJson(updatedUser);
        } else {
            return "User not found or update failed";
        }
    }

    public String deleteUserById(int id) {
        boolean success = userDao.deleteUser(id);
        if (success) {
            return "User deleted";
        } else {
            return "User not found or delete failed";
        }
    }
}
