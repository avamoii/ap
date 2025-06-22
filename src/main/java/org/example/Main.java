package org.example;

import static spark.Spark.*;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.HibernateUtil;
import java.util.logging.LogManager;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.example.controller.AuthenticationController;
import org.example.DAO.UserDao;
import org.example.service.UserService;
import org.example.util.JwtUtil;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        port(8080);
        LogManager.getLogManager().reset();
        // Load .env and set as system properties
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));


        try {
            HibernateUtil.getSessionFactory();//اماده سازی هایبرنیت برای استفاده
            System.out.println("Hibernate SessionFactory initialized successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        Gson gson = new Gson();
        UserDao userDao = new UserDao();
        UserService userService = new UserService(userDao);
        AuthenticationController authController = new AuthenticationController(userService, gson);

//================================================================================================================================
// ================================================================================================================================
        // --- JWT Authentication Filter ---
        before((request, response) -> {
            String path = request.pathInfo();//مسیر درخواست فعلی رو میگیره
            // مسیرهایی که نیاز به توکن ندارند (مثل /auth/register, /auth/login) را مستثنی کنید
            if (path.equals("/auth/register") || path.equals("/auth/login")) {
                return; // نیازی به بررسی توکن نیست
            }


            // مثال: تمام مسیرهای تحت /api/* یا مسیر خاص /auth/profile نیاز به توکن دارند
            if (path.startsWith("/api/") || path.equals("/auth/profile")) {
                String authHeader = request.headers("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    response.type("application/json");
                    halt(401, gson.toJson(Map.of("error", "Unauthorized: Missing or invalid Authorization header")));
                    return; // توقف اجرای فیلتر و درخواست
                }

                String token = authHeader.substring(7); // حذف "Bearer "
                Claims claims; // تعریف متغیر Claims

                try {
                    // فراخوانی متد verifyToken از JwtUtil شما
                    claims = JwtUtil.verifyToken(token);

                } catch (RuntimeException e) { // یا JwtException اگر JwtUtil آن را مستقیم پرتاب می‌کند و شما آن را catch می‌کنید
                    // اگر verifyToken در JwtUtil شما در صورت خطا Exception پرتاب می‌کند
                    response.type("application/json");
                    System.err.println("JWT Verification failed in filter: " + e.getMessage()); // لاگ کردن خطا برای دیباگ
                    // پیام خطا از خود Exception گرفته می‌شود
                    halt(401, gson.toJson(Map.of("error", "Unauthorized: " + e.getMessage())));
                    return; // توقف اجرای فیلتر و درخواست
                }

                // اگر به اینجا رسیدیم یعنی توکن معتبر بوده و claims مقداردهی شده است.
                // حالا userId و userRole را از claims استخراج می‌کنیم.
                // فرض می‌کنیم subject در توکن همان userId است و role یک claim جداگانه است.
                // این بخش را با توجه به نحوه تولید توکن در JwtUtil.generateToken خودتان تنظیم کنید.
                request.attribute("userId", Long.parseLong(claims.getSubject()));
                request.attribute("userRole", claims.get("role", String.class));

                System.out.println("Token validated for user: " + request.attribute("userId") + ", Role: " + request.attribute("userRole"));
            }
        });
//================================================================================================================================
//================================================================================================================================


        // Auth endpoints
        post("/auth/register", authController::handleRegistration);
        // post("/auth/login", authController::handleLogin); // TODO: Implement and uncomment

        get("/auth/profile", (request, response) -> { // این مسیر حالا توسط فیلتر محافظت می‌شود
            response.type("application/json");
            Long userId = request.attribute("userId");
            if (userId == null) { // اگر به هر دلیلی userId در attribute نبود (نباید اتفاق بیفتد اگر فیلتر درست کار کند)
                response.status(403);
                return gson.toJson(Map.of("error", "Forbidden: User ID not found in token"));
            }
            // TODO: منطق دریافت پروفایل کاربر با userId از سرویس
            return gson.toJson(Map.of("message", "Profile for user ID: " + userId + " (Role: " + request.attribute("userRole") + ")"));
        });
        put("/auth/profile", (request, response) -> "Hello World! Update Profile to be implemented."); // TODO
        post("/auth/logout", (request, response) -> "Hello World! Logout to be implemented."); // TODO


        // ... سایر Endpoint های شما که قبلاً تعریف کرده بودید ...
        // Restaurant endpoints
        post("/restaurants", (request, response) -> "Hello World!");
        get("/restaurants/mine", (request, response) -> "Hello World!");
        put("/restaurants/:id", (request, response) -> "Hello World!");
        post("/restaurants/:id/item", (request, response) -> "Hello World!");
        put("/restaurants/:id/item/:item_id", (request, response) -> "Hello World!");
        delete("/restaurants/:id/item/:item_id", (request, response) -> "Hello World!");
        post("/restaurants/:id/menu", (request, response) -> "Hello World!");
        delete("/restaurants/:id/menu/:title", (request, response) -> "Hello World!");
        put("/restaurants/:id/menu/:title", (request, response) -> "Hello World!");
        delete("/restaurants/:id/menu/:title/:item_id", (request, response) -> "Hello World!");
        get("/restaurants/:id/orders", (request, response) -> "Hello World!");
        patch("/restaurants/orders/:order_id", (request, response) -> "Hello World!");

        // Buyer endpoints
        post("/vendors", (request, response) -> "Hello World!"); // این باید احتمالا get باشد
        get("/vendors/:id", (request, response) -> "Hello World!");
        post("/items", (request, response) -> "Hello World!"); // این هم احتمالا get
        get("/items/:id", (request, response) -> "Hello World!");
        get("/coupons", (request, response) -> "Hello World!");
        post("/orders", (request, response) -> "Hello World!");
        get("/orders/:id", (request, response) -> "Hello World!");
        get("/orders/history", (request, response) -> "Hello World!");
        get("/favorites", (request, response) -> "Hello World!");
        put("/favorites/:restaurantId", (request, response) -> "Hello World!");
        delete("/favorites/:restaurantId", (request, response) -> "Hello World!");
        post("/ratings", (request, response) -> "Hello World!");
        get("/ratings/items/:item_id", (request, response) -> "Hello World!");
        get("/ratings/:id", (request, response) -> "Hello World!");
        delete("/ratings/:id", (request, response) -> "Hello World!");
        put("/ratings/:id", (request, response) -> "Hello World!");

        // Courier endpoints
        get("/deliveries/available", (request, response) -> "Hello World!");
        patch("/deliveries/:order_id", (request, response) -> "Hello World!");
        get("/deliveries/history", (request, response) -> "Hello World!");

        // Order endpoints
        get("/transactions", (request, response) -> "Hello World!");
        post("/wallet/top-up", (request, response) -> "Hello World!");
        // post("/payment/online", (request, response) -> "Hello World!"); // فاز ۲

        // Admin endpoints
        get("/admin/users", (request, response) -> "Hello World!");
        patch("/admin/users/:id/status", (request, response) -> "Hello World!");
        get("/admin/orders", (request, response) -> "Hello World!");
        get("/admin/transactions", (request, response) -> "Hello World!");
        get("/admin/coupons", (request, response) -> "Hello World!");
        post("/admin/coupons", (request, response) -> "Hello World!");
        delete("/admin/coupons/:id", (request, response) -> "Hello World!");
        put("/admin/coupons/:id", (request, response) -> "Hello World!");
        get("/admin/coupons/:id", (request, response) -> "Hello World!");


        System.out.println("Server started on port 8080. Endpoints are configured.");
    }
}