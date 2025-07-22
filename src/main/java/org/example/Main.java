package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import org.example.actions.auth.*;
import org.example.actions.buyer.*;
import org.example.actions.courier.*;
import org.example.actions.payment.MakePaymentAction;
import org.example.actions.restaurant.*;
import org.example.actions.transaction.GetTransactionHistoryAction;
import org.example.actions.wallet.TopUpWalletAction;
import org.example.config.HibernateUtil;
import org.example.exception.*;
import org.example.repository.*;
import org.example.util.JwtUtil;
import org.example.actions.admin.*;

import java.util.Map;
import java.util.logging.LogManager;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        // --- Server Configuration & Dependency Injection ---
        port(1214); // Using your specified port
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

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        // --- Dependency Injection: Create one instance of each repository ---
        UserRepository userRepository = new UserRepositoryImpl();
        RestaurantRepository restaurantRepository = new RestaurantRepositoryImpl();
        FoodItemRepository foodItemRepository = new FoodItemRepositoryImpl();
        MenuRepository menuRepository = new MenuRepositoryImpl();
        OrderRepository orderRepository = new OrderRepositoryImpl();
        CouponRepository couponRepository = new CouponRepositoryImpl();
        RatingRepository ratingRepository = new RatingRepositoryImpl();
        TransactionRepository transactionRepository = new TransactionRepositoryImpl();

        // --- Global Filters & Exception Handlers ---
        before((request, response) -> {
            // --- Content-Type Filter
            String method = request.requestMethod();
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

            // A single, unified check for all protected routes with correct spelling
            if (path.startsWith("/auth/") || path.startsWith("/restaurants") || path.startsWith("/vendors") || path.startsWith("/items") || path.startsWith("/coupons") || path.startsWith("/orders") || path.startsWith("/favorites") || path.startsWith("/ratings") || path.startsWith("/deliveries") || path.startsWith("/transactions") || path.startsWith("/wallet")||path.startsWith("/payment")|| path.startsWith("/admin")) {
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

        exception(InvalidInputException.class, (e, request, response) -> { response.status(400); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(UnauthorizedException.class, (e, request, response) -> { response.status(401); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(ForbiddenException.class, (e, request, response) -> { response.status(403); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(NotFoundException.class, (e, request, response) -> { response.status(404); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(ResourceConflictException.class, (e, request, response) -> { response.status(409); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(UnsupportedMediaTypeException.class, (e, request, response) -> { response.status(415); response.type("application/json"); response.body(gson.toJson(Map.of("error", e.getMessage()))); });
        exception(Exception.class, (e, request, response) -> { e.printStackTrace(); response.status(500); response.type("application/json"); response.body(gson.toJson(Map.of("error", "An unexpected internal server error occurred."))); });


        //================================================================================================================
        // --- API Routes ---
        //================================================================================================================

        // --- Auth Endpoints ---
        post("/auth/register", new RegisterUserAction(gson, userRepository));
        post("/auth/login", new LoginUserAction(gson, userRepository));
        get("/auth/profile", new GetUserProfileAction(gson, userRepository));
        put("/auth/profile", new UpdateUserProfileAction(gson, userRepository));
        post("/auth/logout", new LogoutUserAction(gson));

        // --- Restaurant Endpoints ---
        post("/restaurants", new CreateRestaurantAction(gson, userRepository, restaurantRepository));
        get("/restaurants/mine", new GetMyRestaurantsAction(gson, restaurantRepository));
        put("/restaurants/:id", new UpdateRestaurantAction(gson, restaurantRepository));
        post("/restaurants/:id/item", new AddFoodItemAction(gson, restaurantRepository, foodItemRepository));
        put("/restaurants/:id/item/:item_id", new UpdateFoodItemAction(gson, foodItemRepository));
        delete("/restaurants/:id/item/:item_id", new DeleteFoodItemAction(gson, foodItemRepository));
        post("/restaurants/:id/menu", new CreateMenuAction(gson, restaurantRepository, menuRepository));
        delete("/restaurants/:id/menu/:title/:item_id", new RemoveFoodItemFromMenuAction(gson, menuRepository));
        get("/restaurants/:id/orders", new GetRestaurantOrdersAction(gson, restaurantRepository, orderRepository));
        patch("/restaurants/orders/:order_id", new UpdateOrderStatusAction(gson, orderRepository));

        // --- Buyer Endpoints ---
        post("/vendors", new ListVendorsAction(gson, restaurantRepository));
        get("/vendors/:id", new GetVendorMenuAction(gson, restaurantRepository));
        post("/items", new ListItemsAction(gson, foodItemRepository));
        get("/items/:id", new GetItemDetailsAction(gson, foodItemRepository));
        get("/coupons", new CheckCouponAction(gson, couponRepository));

        // --- Favorites Endpoints ---
        get("/favorites", new GetFavoritesAction(gson, userRepository));
        put("/favorites/:restaurantId", new AddFavoriteRestaurantAction(gson, userRepository, restaurantRepository));
        delete("/favorites/:restaurantId", new RemoveFavoriteRestaurantAction(gson, userRepository));

        // --- Order Endpoints ---
        post("/orders", new SubmitOrderAction(gson, userRepository, restaurantRepository, foodItemRepository, orderRepository, couponRepository));
        get("/orders/:id", new GetOrderDetailsAction(gson, orderRepository));
        get("/orders/history", new GetOrderHistoryAction(gson, orderRepository));

        // --- Rating Endpoints ---
        post("/ratings", new SubmitRatingAction(gson, orderRepository, ratingRepository));
        get("/ratings/items/:item_id", new GetItemRatingsAction(gson, ratingRepository));
        get("/ratings/:id", new GetRatingDetailsAction(gson, ratingRepository));
        put("/ratings/:id", new UpdateRatingAction(gson, ratingRepository));
        delete("/ratings/:id", new DeleteRatingAction(gson, ratingRepository));

        // --- Courier Endpoints ---
        get("/deliveries/available", new GetAvailableDeliveriesAction(gson, orderRepository));
        patch("/deliveries/:order_id", new UpdateDeliveryStatusAction(gson, orderRepository, userRepository));
        get("/deliveries/history", new GetDeliveryHistoryAction(gson, orderRepository));

        // --- Transaction & Wallet Endpoints ---
        get("/transactions", new GetTransactionHistoryAction(gson, transactionRepository));
        post("/wallet/top-up", new TopUpWalletAction(gson, userRepository, transactionRepository));
        post("/payment/online", new MakePaymentAction(gson, orderRepository, userRepository, transactionRepository));
        //--- Admin Endpoints ---
        get("/admin/users", new ListUsersAdminAction(gson, userRepository));
        patch("/admin/users/:id/status", new UpdateUserStatusAction(gson, userRepository));
        get("/admin/orders", new ListOrdersAdminAction(gson, orderRepository));
        get("/admin/transactions", new ListTransactionsAdminAction(gson, transactionRepository));
        get("/admin/coupons",new ListCouponsAdminAction(gson, couponRepository));
        post("/admin/coupons", new CreateCouponAction(gson, couponRepository));
        System.out.println("Server started on port 1214. Endpoints are configured.");
    }
}
