package org.example;
import static spark.Spark.*;
import org.example.config.HibernateUtil;
import org.hibernate.Session;
import java.util.logging.LogManager;
import org.example.controller.UserController;


public class Main {
    public static void main(String[] args) {
        port(8080);
        LogManager.getLogManager().reset(); // disable logging spam from Hibernate

        // Auth endpoints
        post("/auth/register", (request, response) -> "Hello World!");
        post("/auth/login", (request, response) -> "Hello World!");
        get("/auth/profile", (request, response) -> "Hello World!");
        put("/auth/profile", (request, response) -> "Hello World!");
        post("/auth/logout", (request, response) -> "Hello World!");

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
        post("/vendors", (request, response) -> "Hello World!");
        get("/vendors/:id", (request, response) -> "Hello World!");
        post("/items", (request, response) -> "Hello World!");
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
        post("/payment/online", (request, response) -> "Hello World!");

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


    }
}
