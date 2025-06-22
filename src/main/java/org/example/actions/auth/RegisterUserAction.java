package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.config.HibernateUtil;
import org.example.dto.RegisterRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.ResourceConflictException;
import org.example.model.User;
import org.example.util.JwtUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.HashMap;
import java.util.Map;

public class RegisterUserAction implements Route {

    private final Gson gson;

    public RegisterUserAction(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

        //400
        if (registerRequest.getPhoneNumber() == null || registerRequest.getPassword() == null ||
                registerRequest.getFirstName() == null || registerRequest.getLastName() == null ||
                registerRequest.getRole() == null) {
            throw new InvalidInputException("Missing required fields");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // 409
            User existingUser = session.createQuery("FROM User WHERE phoneNumber = :phoneNumber", User.class)
                    .setParameter("phoneNumber", registerRequest.getPhoneNumber())
                    .uniqueResult();

            if (existingUser != null) {
                throw new ResourceConflictException("Phone number already exists");
            }

            // ۳. مسیر موفقیت آمیز: ساختن کاربر جدید
            User newUser = new User();
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setPhoneNumber(registerRequest.getPhoneNumber());
            newUser.setRole(registerRequest.getRole());
            newUser.setAddress(registerRequest.getAddress());
            newUser.setPassword(registerRequest.getPassword());

            session.persist(newUser);
            transaction.commit();

            // ۴. ساختن توکن و پاسخ نهایی
            String token = JwtUtil.generateToken(newUser.getId(), newUser.getRole().toString());
            response.status(200); // Created

            UserDTO userDto = new UserDTO(
                    newUser.getId(), newUser.getFirstName(), newUser.getLastName(),
                    newUser.getPhoneNumber(), newUser.getRole(), newUser.getAddress()
            );
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", userDto);
            responseData.put("token", token);
            return gson.toJson(responseData);

        } catch (Exception e) {
            // ۵. مدیریت خطای پیش‌بینی نشده (این بلوک برای خطاهای دیتابیس و ... باقی می‌ماند)
            e.printStackTrace();
            throw new RuntimeException("An unexpected internal error occurred");
        }
    }
}