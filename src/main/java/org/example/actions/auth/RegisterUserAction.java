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

        // 400 - اعتبارسنجی برای فیلد جدید
        if (registerRequest.getFullName() == null || registerRequest.getPhone() == null || // <--- تغییر
                registerRequest.getPassword() == null || registerRequest.getRole() == null) {
            throw new InvalidInputException("Missing required fields");
        }

        // 409
        Optional<User> existingUser = userRepository.findByPhoneNumber(registerRequest.getPhone());
        if (existingUser.isPresent()) {
            throw new ResourceConflictException("Phone number already exists");
        }

        // ساختن کاربر جدید
        User newUser = new User();

        // <--- تغییر: تفکیک کردن نام کامل به نام کوچک و بزرگ --->
        String fullName = registerRequest.getFullName();
        String[] names = fullName.trim().split("\\s+", 2); // بر اساس اولین فاصله، به حداکثر دو بخش تقسیم می‌کند
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

        // تبدیل BankInfoDTO به BankInfo Entity
        if (registerRequest.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(registerRequest.getBankInfo().getBankName());
            bankInfo.setAccountNumber(registerRequest.getBankInfo().getAccountNumber());
            newUser.setBankInfo(bankInfo);
        }

        // ذخیره کاربر جدید از طریق ریپازیتوری
        User savedUser = userRepository.save(newUser);

        // ساختن توکن و پاسخ نهایی
        String token = JwtUtil.generateToken(savedUser.getId(), savedUser.getRole().toString());
        response.status(200);

        UserDTO userDto = new UserDTO(
                savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(),
                savedUser.getPhoneNumber(), savedUser.getRole(), savedUser.getAddress()
        );
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", userDto);
        responseData.put("token", token);
        return gson.toJson(responseData);
    }

}