// 10. Main Server Class
package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.HibernateUtil;
import org.example.controller.admin.*;
import org.example.controller.auth.*;
import org.example.controller.TestController;
import org.example.controller.buyer.*;
import org.example.controller.payment.MakePaymentController;
import org.example.controller.restaurant.*;
import org.example.controller.transaction.GetTransactionHistoryController;
import org.example.controller.wallet.TopUpWalletController;
import org.example.core.Router;
import org.example.core.ServerHandler;
import org.example.middleware.AuthMiddleware;
import org.example.middleware.ContentTypeMiddleware;
import org.example.repository.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.LogManager;

public class HttpMain {
    public static void main(String[] args) throws IOException {
        // Server Configuration
        LogManager.getLogManager().reset();

        // Load environment variables
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // read port from env file
        int port = Integer.parseInt(System.getProperty("SERVER_PORT", "8080"));

        // Initialize Hibernate
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate SessionFactory initialized successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        // Create Gson instance
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // Initialize repositories
        UserRepository userRepository = new UserRepositoryImpl();
        RestaurantRepository restaurantRepository = new RestaurantRepositoryImpl();
        FoodItemRepository foodItemRepository = new FoodItemRepositoryImpl();
        MenuRepository menuRepository = new MenuRepositoryImpl();
        OrderRepository orderRepository = new OrderRepositoryImpl();
        CouponRepository couponRepository = new CouponRepositoryImpl();
        RatingRepository ratingRepository = new RatingRepositoryImpl();
        TransactionRepository transactionRepository = new TransactionRepositoryImpl();

        // Setup router with middlewares
        Router router = new Router()
                .use(new ContentTypeMiddleware())
                .use(new AuthMiddleware());

        // Register routes
        setupRoutes(router, gson, userRepository, restaurantRepository, foodItemRepository,
                menuRepository, orderRepository, couponRepository, ratingRepository, transactionRepository);

        // Create and start server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ServerHandler(router, gson));
        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Server started on port " + port);
    }

    private static void setupRoutes(Router router, Gson gson,
                                    UserRepository userRepository,
                                    RestaurantRepository restaurantRepository,
                                    FoodItemRepository foodItemRepository,
                                    MenuRepository menuRepository,
                                    OrderRepository orderRepository,
                                    CouponRepository couponRepository,
                                    RatingRepository ratingRepository,
                                    TransactionRepository transactionRepository) {

        // Auth routes
        router.get("/test", new TestController(gson, userRepository));

        setupAuthRoutes(router, gson, userRepository);
        setupRestaurantRoutes(router, gson, userRepository, restaurantRepository,
                foodItemRepository, menuRepository, orderRepository);
        setupBuyerRoutes(router, gson, userRepository, restaurantRepository,
                foodItemRepository, couponRepository, orderRepository, ratingRepository);
        setupPaymentRoutes(router, gson, userRepository, orderRepository, transactionRepository);
        setupAdminRoutes(router, gson, userRepository, orderRepository, transactionRepository, couponRepository);
    }

    private static void setupAuthRoutes(Router router, Gson gson, UserRepository userRepository) {
        // Auth routes
        router.post("/auth/register", new RegisterUserController(gson, userRepository));
        router.post("/auth/login", new LoginUserController(gson, userRepository));
        router.get("/auth/profile", new GetUserProfileController(gson, userRepository));
        router.put("/auth/profile", new UpdateUserProfileController(gson, userRepository));
        router.post("/auth/logout", new LogoutUserController(gson));
    }

    private static void setupRestaurantRoutes(Router router, Gson gson,
                                              UserRepository userRepository,
                                              RestaurantRepository restaurantRepository,
                                              FoodItemRepository foodItemRepository,
                                              MenuRepository menuRepository,
                                              OrderRepository orderRepository) {

        // Restaurant routes
        router.post("/restaurants", new CreateRestaurantController(gson, userRepository, restaurantRepository));
        router.get("/restaurants/mine", new GetMyRestaurantsController(gson, restaurantRepository));
        router.put("/restaurants/:id", new UpdateRestaurantController(gson, restaurantRepository));

        // Menu and food item routes
        router.post("/restaurants/:id/menu/:title/item", new AddFoodItemController(gson, restaurantRepository, foodItemRepository, menuRepository));
        router.put("/restaurants/:id/item/:item_id", new UpdateFoodItemController(gson, foodItemRepository));
        router.delete("/restaurants/:id/item/:item_id", new DeleteFoodItemController(gson, foodItemRepository));
        router.post("/restaurants/:id/menu", new CreateMenuController(gson, restaurantRepository, menuRepository));
        router.delete("/restaurants/:id/menu/:title/:item_id", new RemoveFoodItemFromMenuController(gson, menuRepository));

        // Order management routes
        router.get("/restaurants/:id/orders", new GetRestaurantOrdersController(gson, restaurantRepository, orderRepository));
        router.patch("/restaurants/orders/:order_id", new UpdateOrderStatusController(gson, orderRepository));
    }

    private static void setupBuyerRoutes(Router router, Gson gson,
                                         UserRepository userRepository,
                                         RestaurantRepository restaurantRepository,
                                         FoodItemRepository foodItemRepository,
                                         CouponRepository couponRepository,
                                         OrderRepository orderRepository,
                                         RatingRepository ratingRepository) {

        // Vendor/Restaurant browsing
        router.post("/vendors", new ListVendorsController(gson, restaurantRepository));
        router.get("/vendors/:id", new GetVendorMenuController(gson, restaurantRepository));

        // Item browsing
        router.post("/items", new ListItemsController(gson, foodItemRepository));
        router.get("/items/:id", new GetItemDetailsController(gson, foodItemRepository));

        // Coupon checking
        router.get("/coupons", new CheckCouponController(gson, couponRepository));

        // Favorites management
        router.get("/favorites", new GetFavoritesController(gson, userRepository));
        router.put("/favorites/:restaurantId", new AddFavoriteRestaurantController(gson, userRepository, restaurantRepository));
        router.delete("/favorites/:restaurantId", new RemoveFavoriteRestaurantController(gson, userRepository));

        // Order management
        router.post("/orders", new SubmitOrderController(gson, userRepository, restaurantRepository,
                foodItemRepository, orderRepository, couponRepository));
        router.get("/orders/history", new GetOrderHistoryController(gson, orderRepository));
        router.get("/orders/:id", new GetOrderDetailsController(gson, orderRepository));

        // Rating management
        router.post("/ratings", new SubmitRatingController(gson, orderRepository, ratingRepository));
        router.get("/ratings/items/:item_id", new GetItemRatingsController(gson, ratingRepository));
        router.get("/ratings/:id", new GetRatingDetailsController(gson, ratingRepository));
        router.put("/ratings/:id", new UpdateRatingController(gson, ratingRepository));
        router.delete("/ratings/:id", new DeleteRatingController(gson, ratingRepository));
    }

    private static void setupPaymentRoutes(Router router, Gson gson,
                                           UserRepository userRepository,
                                           OrderRepository orderRepository,
                                           TransactionRepository transactionRepository) {

        // Transaction routes
        router.get("/transactions", new GetTransactionHistoryController(gson, transactionRepository));

        // Wallet routes
        router.post("/wallet/top-up", new TopUpWalletController(gson, userRepository, transactionRepository));

        // Payment routes
        router.post("/payment/online", new MakePaymentController(gson, orderRepository, userRepository, transactionRepository));
    }

    private static void setupAdminRoutes(Router router, Gson gson,
                                         UserRepository userRepository,
                                         OrderRepository orderRepository,
                                         TransactionRepository transactionRepository,
                                         CouponRepository couponRepository) {

        // User management routes
        router.get("/admin/users", new ListUsersAdminController(gson, userRepository));
        router.patch("/admin/users/:id/status", new UpdateUserStatusController(gson, userRepository));

        // Order management routes
        router.get("/admin/orders", new ListOrdersAdminController(gson, orderRepository));

        // Transaction management routes
        router.get("/admin/transactions", new ListTransactionsAdminController(gson, transactionRepository));

        // Coupon management routes
        router.get("/admin/coupons", new ListCouponsAdminController(gson, couponRepository));
        router.post("/admin/coupons", new CreateCouponController(gson, couponRepository));
        router.delete("/admin/coupons/:id", new DeleteCouponController(gson, couponRepository));
        router.put("/admin/coupons/:id", new UpdateCouponController(gson, couponRepository));
        router.get("/admin/coupons/:id", new GetCouponDetailsAdminController(gson, couponRepository));
    }
}