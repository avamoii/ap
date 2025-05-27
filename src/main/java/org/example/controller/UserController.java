package org.example.controller;


import com.google.gson.Gson;
import org.example.dto.RegisterRequest;
import org.example.service.UserService;
import java.util.Map;
import static spark.Spark.*;
public class UserController {

    private static final Gson gson = new Gson();
    private static final UserService userService = new UserService();





