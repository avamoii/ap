package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import org.example.actions.auth.GetUserProfileAction;
import org.example.actions.auth.LoginUserAction;
import org.example.actions.auth.RegisterUserAction;
import org.example.config.HibernateUtil;
import org.example.exception.*;
import org.example.repository.UserRepository;
import org.example.repository.UserRepositoryImpl;
import org.example.util.JwtUtil;

import java.util.Map;
import java.util.logging.LogManager;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        // --- Server and Environment Configuration ---
        port(1234);
        LogManager.getLogManager().reset(); // Optional: Resets logging configuration.

        // Load .env file and set variables as system properties
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // --- Database Initialization ---
        try {
            HibernateUtil.getSessionFactory(); // Initialize Hibernate SessionFactory on startup
            System.out.println("Hibernate SessionFactory initialized successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        // --- Gson Configuration ---
        // Configure Gson to automatically convert snake_case (from JSON) to camelCase (in Java)
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // --- Dependency Injection ---
        // Create a single instance of the repository to be used by all actions
        UserRepository userRepository = new UserRepositoryImpl();


        //================================================================================================================
        // --- Global Filters (Executed before each request) ---
        //================================================================================================================

        before((request, response) -> {
            // --- Content-Type Filter for 415 Unsupported Media Type ---
            String method = request.requestMethod();
            // This check applies only to methods that can have a request body
            if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
                if (request.contentType() == null || !request.contentType().equalsIgnoreCase("application/json")) {
                    throw new UnsupportedMediaTypeException("Unsupported Media Type: Only application/json is supported.");
                }
            }

            // --- JWT Authentication Filter ---
            String path = request.pathInfo();
            // Exclude public paths that do not require a token
            if (path.equals("/auth/register") || path.equals("/auth/login")) {
                return; // Stop filter execution for public routes
            }

            // Secure all routes starting with /api/ or specific protected routes like /auth/profile
            if (path.startsWith("/api/") || path.equals("/auth/profile")) {
                String authHeader = request.headers("Authorization");

                // 1. If the header is missing or invalid, throw an UnauthorizedException
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new UnauthorizedException("Unauthorized: Missing or invalid Authorization header");
                }

                String token = authHeader.substring(7); // Remove "Bearer " prefix

                // 2. Verify the token. If invalid, JwtUtil will throw an UnauthorizedException itself.
                // No try-catch block is needed here because the global exception handler will catch it.
                Claims claims = JwtUtil.verifyToken(token);

                // If the token is valid, add user info to the request attributes for later use in actions
                request.attribute("userId", Long.parseLong(claims.getSubject()));
                request.attribute("userRole", claims.get("role", String.class));
            }
        });


        //================================================================================================================
        // --- Global Exception Handlers ---
        //================================================================================================================

        // Handles validation errors (e.g., missing fields)
        exception(InvalidInputException.class, (e, request, response) -> {
            response.status(400); // Bad Request
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // Handles authentication errors (e.g., invalid token, wrong password)
        exception(UnauthorizedException.class, (e, request, response) -> {
            response.status(401); // Unauthorized
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // Handles resource not found errors
        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404); // Not Found
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // Handles conflicts (e.g., creating a user that already exists)
        exception(ResourceConflictException.class, (e, request, response) -> {
            response.status(409); // Conflict
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // Handles requests with an unsupported media type
        exception(UnsupportedMediaTypeException.class, (e, request, response) -> {
            response.status(415); // Unsupported Media Type
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        // A catch-all handler for any other unexpected exceptions
        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace(); // Log the full stack trace for debugging
            response.status(500); // Internal Server Error
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", "An unexpected internal server error occurred.")));
        });


        //================================================================================================================
        // --- API Routes ---
        //================================================================================================================

        // --- Auth Endpoints ---
        post("/auth/register", new RegisterUserAction(gson, userRepository));
        post("/auth/login", new LoginUserAction(gson, userRepository));

        get("/auth/profile", new GetUserProfileAction(gson, userRepository));
        put("/auth/profile", (request, response) -> "TODO: Update Profile to be implemented.");
        post("/auth/logout", (request, response) -> "TODO: Logout to be implemented.");


        // --- Restaurant Endpoints (Placeholders) ---
        post("/restaurants", (request, response) -> "TODO: Create Restaurant");
        // ... other placeholder routes

        System.out.println("Server started on port 1234. Endpoints are configured.");
    }
}
