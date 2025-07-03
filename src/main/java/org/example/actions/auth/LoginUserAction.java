package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.dto.LoginRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.UnauthorizedException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.JwtUtil;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginUserAction implements Route {
    private final Gson gson;
    private final UserRepository userRepository;

    public LoginUserAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

        //400
        if (loginRequest.getPhone() == null || loginRequest.getPhone().trim().isEmpty()) {
            throw new InvalidInputException("Invalid `phone`");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new InvalidInputException("Invalid `password`");
        }

        // ۲. پیدا کردن کاربر از طریق ریپازیتوری
        Optional<User> userOptional = userRepository.findByPhoneNumber(loginRequest.getPhone());

        // ===> ۳. تفکیک خطای 404 از 401 <===
        // اگر کاربر وجود نداشت، خطای 404 پرتاب کن
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User with this phone number not found");
        }

        // حالا که مطمئنیم کاربر وجود دارد، آن را از Optional خارج می‌کنیم
        User user = userOptional.get();

        // اگر رمز عبور اشتباه بود، خطای 401 پرتاب کن
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }
        String token = JwtUtil.generateToken(user.getId(), user.getRole().toString());

        UserDTO userDto = new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getAddress()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "User logged in successfully");
        responseData.put("token", token);
        responseData.put("user", userDto);

        response.status(200);
        return gson.toJson(responseData);
    }
}