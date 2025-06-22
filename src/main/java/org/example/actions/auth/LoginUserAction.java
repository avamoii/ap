// File: src/main/java/org/example/actions/auth/LoginUserAction.java
package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.config.HibernateUtil;
//import org.example.dto.LoginRequest;
import org.example.dto.UserDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.UnauthorizedException;
import org.example.model.User;
import org.example.util.JwtUtil;
import org.hibernate.Session;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

//public class LoginUserAction implements Route {
//    private final Gson gson;
//
//    public LoginUserAction(Gson gson) {
//        this.gson = gson;
//    }
//    }
    //   @Override
//    public Object handle(Request request, Response response) {
//        response.type("application/json");
//        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
//
//        // 1. اعتبارسنجی ورودی (برای خطای 400)
//        if (loginRequest.getPhone() == null || loginRequest.getPhone().trim().isEmpty()) {
//            throw new InvalidInputException("`phone` field is required");
//        }
//        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
//            throw new InvalidInputException("`password` field is required");
//        }
//
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            // 2. پیدا کردن کاربر
//            User user = session.createQuery("FROM User WHERE phoneNumber = :phone", User.class)
//                    .setParameter("phone", loginRequest.getPhone())
//                    .uniqueResult();
//
//            // 3. بررسی صحت اطلاعات (برای خطای 401)
//            if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
//                throw new UnauthorizedException("Invalid phone number or password");
//            }
//
//            // 4. مسیر موفقیت آمیز (پاسخ 200)
//            String token = JwtUtil.generateToken(user.getId(), user.getRole().toString());
//            UserDTO userDto = new UserDTO(/* ... */); // یوزر DTO را پر کنید
//            Map<String, Object> responseData = new HashMap<>();
//            responseData.put("message", "User logged in successfully");
//            responseData.put("token", token);
//            responseData.put("user", userDto);
//
//            response.status(200);
//            return gson.toJson(responseData);
//
//        } catch (Exception e) {
//            // 5. مدیریت خطای پیش‌بینی نشده (برای خطای 500)
//            e.printStackTrace(); // این برای دیباگ کردن خودتان است
//            // یک خطای عمومی پرتاب می‌کنیم تا هندلر سراسری آن را بگیرد
//            throw new RuntimeException("An internal server error occurred. Please try again later.");
//        }
//    }
//}