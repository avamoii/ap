package org.example.service;

import org.example.DAO.UserDao;
import org.example.model.User;
import org.example.dto.RegisterRequest;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User registerUser(RegisterRequest registerRequest) throws Exception {
        if (userDao.findByPhoneNumber(registerRequest.getPhoneNumber()) != null) {
            throw new Exception("Phone number already exists: " + registerRequest.getPhoneNumber());
        }

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setPhoneNumber(registerRequest.getPhoneNumber());
        newUser.setRole(registerRequest.getRole());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setPassword(registerRequest.getPassword()); // پسورد خام ذخیره می‌شود

        return userDao.save(newUser);
    }

    public User loginUser(String phoneNumber, String rawPassword) throws Exception {
        User user = userDao.findByPhoneNumber(phoneNumber);
        if (user == null || !user.getPassword().equals(rawPassword)) {
            throw new Exception("Invalid phone number or password");
        }
        return user;
    }
}