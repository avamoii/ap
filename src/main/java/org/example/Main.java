package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import org.example.actions.auth.GetUserProfileAction;
import org.example.actions.auth.LoginUserAction;
import org.example.actions.auth.LogoutUserAction;
import org.example.actions.auth.RegisterUserAction;
import org.example.actions.auth.UpdateUserProfileAction;
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
        LogManager.getLogManager().reset();

        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // --- Database Initialization ---
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate SessionFactory initialized successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        // --- Gson Configuration ---
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // --- Dependency Injection ---
        UserRepository userRepository = new UserRepositoryImpl();


        //================================================================================================================
        // --- Global Filters (Executed before each request) ---
        //================================================================================================================

        before((request, response) -> {
            // --- Content-Type Filter for 415 Unsupported Media Type ---
            String method = request.requestMethod();

            // This check applies only to methods that can have a request body AND actually have content.
            // A request with no body might have a content length of 0 or -1.
            if ((method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) && request.contentLength() > 0) {
                if (request.contentType() == null || !request.contentType().equalsIgnoreCase("application/json")) {
                    throw new UnsupportedMediaTypeException("Unsupported Media Type: Only application/json is supported.");
                }
            }

            // --- JWT Authentication Filter ---
            String path = request.pathInfo();
            if (path.equals("/auth/register") || path.equals("/auth/login")) {
                return;
            }

            if (path.startsWith("/api/") || path.equals("/auth/profile") || path.equals("/auth/logout")) {
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


        //================================================================================================================
        // --- Global Exception Handlers ---
        //================================================================================================================

        exception(InvalidInputException.class, (e, request, response) -> {
            response.status(400);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(UnauthorizedException.class, (e, request, response) -> {
            response.status(401);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(ForbiddenException.class, (e, request, response) -> {
            response.status(403);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(ResourceConflictException.class, (e, request, response) -> {
            response.status(409);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(UnsupportedMediaTypeException.class, (e, request, response) -> {
            response.status(415);
            response.type("application/json");
            response.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace();
            response.status(500);
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
        put("/auth/profile", new UpdateUserProfileAction(gson, userRepository));
        post("/auth/logout", new LogoutUserAction(gson));

        // ... other placeholder routes

        System.out.println("Server started on port 1234. Endpoints are configured.");
    }
}
