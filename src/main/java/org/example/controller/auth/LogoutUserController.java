// 5. LOGOUT USER CONTROLLER
package org.example.controller.auth;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;

import java.util.Map;

/**
 * Controller to handle the POST /auth/logout request.
 * In a stateless JWT system, this endpoint primarily serves as a confirmation.
 * The main logout logic (deleting the token) happens on the client side.
 */
public class LogoutUserController extends BaseController {

    public LogoutUserController(Gson gson) {
        super(gson);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // The AuthMiddleware has already authenticated the user.
        // There is no server-side state to clear for a JWT.
        // We simply return a success message to acknowledge the client's request.

        Long userId = getCurrentUserId();
        // Optional: You could log the logout event for auditing purposes.
        // logger.info("User with ID {} has logged out.", userId);

        response.status(200);
        sendJson(response, Map.of("message", "User logged out successfully"));
    }
}