package org.example;
import org.example.exception.InvalidInputException;
import org.example.exception.ResourceConflictException;
import org.example.exception.UnauthorizedException;
import static spark.Spark.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.actions.auth.RegisterUserAction;
import org.example.config.HibernateUtil;
import java.util.logging.LogManager;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.example.util.JwtUtil;
import java.util.Map;
import org.example.repository.UserRepository;
import org.example.repository.UserRepositoryImpl;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;// <-- این را هم ایمپورت کنید

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

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

//================================================================================================================================
// ================================================================================================================================
        // --- JWT Authentication Filter ---
        before((request, response) -> {
            String path = request.pathInfo();
            if (path.equals("/auth/register") || path.equals("/auth/login")) {
                return;
            }

            if (path.startsWith("/api/") || path.equals("/auth/profile")) {
                String authHeader = request.headers("Authorization");

                // ۱. اگر هدر وجود نداشت، مستقیماً Exception پرتاب کن
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new UnauthorizedException("Unauthorized: Missing or invalid Authorization header");
                }

                String token = authHeader.substring(7);

                // ۲. اینجا دیگر نیازی به try-catch نیست!
                // متد verifyToken را فراخوانی کن. اگر توکن نامعتبر باشد، خودش
                // UnauthorizedException پرتاب می‌کند و هندلر سراسری ما در Main.java آن را می‌گیرد.
                Claims claims = JwtUtil.verifyToken(token);

                // اگر کد به اینجا برسد، یعنی توکن معتبر بوده است.
                request.attribute("userId", Long.parseLong(claims.getSubject()));
                request.attribute("userRole", claims.get("role", String.class));
            }
        });
// ================================================================================================================================
        UserRepository userRepository = new UserRepositoryImpl();
        // ================== HANDLER های سراسری برای EXCEPTION ها ==================

        // این هندلر زمانی اجرا می‌شود که یک InvalidInputException پرتاب شود
        exception(InvalidInputException.class, (e, request, response) -> {
            response.status(400); // Bad Request
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // این هندلر برای خطای Unauthorized اجرا می‌شود
        exception(UnauthorizedException.class, (e, request, response) -> {
            response.status(401); // Unauthorized
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // این هندلر برای خطای ResourceConflictException اجرا می‌شود
        exception(ResourceConflictException.class, (e, request, response) -> {
            response.status(409); // Conflict
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // این هندلر به عنوان آخرین راه حل، برای تمام خطاهای پیش‌بینی نشده دیگر اجرا می‌شود
        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace(); // برای دیباگ
            response.status(500); // Internal Server Error
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });



        // ================================================================================================================================
        post("/auth/register", new RegisterUserAction(gson, userRepository)); // این اکشن حالا از ریپازیتوری استفاده می‌کند
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