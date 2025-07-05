package org.example.actions.auth;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Action to handle the POST /auth/logout request.
 * In a stateless JWT system, this endpoint primarily serves as a confirmation.
 * The main logout logic (deleting the token) happens on the client side.
 */
public class LogoutUserAction implements Route {

    private final Gson gson;

    public LogoutUserAction(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        // The 'before' filter has already authenticated the user.
        // There is no server-side state to clear for a JWT.
        // We simply return a success message to acknowledge the client's request.

        Long userId = request.attribute("userId");
        // Optional: You could log the logout event for auditing purposes.
        // logger.info("User with ID {} has logged out.", userId);

        response.status(200); // OK
        return gson.toJson(Map.of("message", "User logged out successfully"));
    }
}
