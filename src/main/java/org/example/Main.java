package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import org.example.actions.auth.*;
import org.example.actions.restaurant.CreateRestaurantAction; // 1. ایمپورت کردن اکشن جدید
import org.example.config.HibernateUtil;
import org.example.exception.*;
import org.example.repository.RestaurantRepository;         // 1. ایمپورت کردن ریپازیتوری جدید
import org.example.repository.RestaurantRepositoryImpl;      // 1. ایمپورت کردن ریپازیتوری جدید
import org.example.repository.UserRepository;
import org.example.repository.UserRepositoryImpl;
import org.example.util.JwtUtil;

import java.util.Map;
import java.util.logging.LogManager;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(1234);
        LogManager.getLogManager().reset();
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate SessionFactory initialized successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // --- Dependency Injection ---
        UserRepository userRepository = new UserRepositoryImpl();
        RestaurantRepository restaurantRepository = new RestaurantRepositoryImpl(); // 2. ساختن نمونه از ریپازیتوری رستوران

        // --- Global Filters ---
        before((request, response) -> {
            // ... (فیلتر Content-Type بدون تغییر)

            // --- JWT Authentication Filter ---
            String path = request.pathInfo();
            if (path.equals("/auth/register") || path.equals("/auth/login")) {
                return;
            }

            // 3. اضافه کردن مسیر رستوران به لیست مسیرهای محافظت‌شده
            if (path.startsWith("/auth/") || path.startsWith("/restaurants")) {
                String authHeader = request.headers("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new UnauthorizedException("Unauthorized: Missing or invalid Authorization header");
                }
                String token = authHeader.substring(7);
                Claims claims = JwtUtil.verifyToken(token);
                request.attribute("userId", Long.parseLong(claims.getSubject()));
                request.attribute("userRole", claims.get("role", String.class));
            }
        });

        // --- Global Exception Handlers ---
        exception(ForbiddenException.class, (e, request, response) -> {
            response.status(403); // Forbidden
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });
        // ... (سایر Handler ها بدون تغییر)


        //================================================================================================================
        // --- API Routes ---
        //================================================================================================================

        // --- Auth Endpoints ---
        post("/auth/register", new RegisterUserAction(gson, userRepository));
        post("/auth/login", new LoginUserAction(gson, userRepository));
        get("/auth/profile", new GetUserProfileAction(gson, userRepository));
        put("/auth/profile", new UpdateUserProfileAction(gson, userRepository));
        post("/auth/logout", new LogoutUserAction(gson));

        // --- DEBUGGING ENDPOINT ---
        get("/auth/debug-token", (request, response) -> {
            response.type("application/json");
            Long userIdFromToken = request.attribute("userId");
            String userRoleFromToken = request.attribute("userRole");
            return gson.toJson(Map.of(
                    "message", "Data extracted from your token",
                    "userId", userIdFromToken,
                    "userRole", userRoleFromToken
            ));
        });

        // --- Restaurant Endpoints ---
        post("/restaurants", new CreateRestaurantAction(gson, userRepository, restaurantRepository));

        // ...
    }
}
