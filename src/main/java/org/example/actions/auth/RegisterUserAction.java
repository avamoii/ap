package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.dto.RegisterRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.ResourceConflictException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.JwtUtil;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.model.BankInfo;

public class RegisterUserAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public RegisterUserAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

        if (registerRequest.getFullName() == null || registerRequest.getPhone() == null ||
                registerRequest.getPassword() == null || registerRequest.getRole() == null) {
            throw new InvalidInputException("Missing required fields");
        }

        Optional<User> existingUser = userRepository.findByPhoneNumber(registerRequest.getPhone());
        if (existingUser.isPresent()) {
            throw new ResourceConflictException("Phone number already exists");
        }

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            Optional<User> existingEmailUser = userRepository.findByEmail(registerRequest.getEmail());
            if (existingEmailUser.isPresent()) {
                throw new ResourceConflictException("Email already exists");
            }
        }

        User newUser = new User();
        String fullName = registerRequest.getFullName();
        String[] names = fullName.trim().split("\\s+", 2);
        String firstName = names.length > 0 ? names[0] : "";
        String lastName = names.length > 1 ? names[1] : "";

        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPhoneNumber(registerRequest.getPhone());
        newUser.setRole(registerRequest.getRole());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setProfileImageBase64(registerRequest.getProfileImageBase64());

        if (registerRequest.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(registerRequest.getBankInfo().getBankName());
            bankInfo.setAccountNumber(registerRequest.getBankInfo().getAccountNumber());
            newUser.setBankInfo(bankInfo);
        }

        User savedUser = userRepository.save(newUser);

        String token = JwtUtil.generateToken(savedUser.getId(), savedUser.getRole().toString());
        response.status(200);

        // --- تغییر اصلی اینجاست ---
        // از سازنده جدید UserDTO استفاده می‌کنیم که فقط آبجکت User را می‌گیرد
        UserDTO userDto = new UserDTO(savedUser);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", userDto);
        responseData.put("token", token);
        return gson.toJson(responseData);
    }
}
