// 10. Main Server Class
package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.HibernateUtil;
import org.example.controller.auth.GetUserProfileController;
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
        router.get("/auth/profile", new GetUserProfileController(gson, userRepository));

        // Add all other routes here following the same pattern...
        // router.post("/auth/register", new RegisterUserController(gson, userRepository));
        // router.post("/auth/login", new LoginUserController(gson, userRepository));
        // etc.
    }
}