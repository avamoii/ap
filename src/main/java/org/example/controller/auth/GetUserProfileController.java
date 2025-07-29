// 9. Refactored Controller Example
package org.example.controller.auth;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.UserDTO;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;

public class GetUserProfileController extends BaseController {
    private final UserRepository userRepository;

    public GetUserProfileController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        // Get user ID from context (set by AuthMiddleware)
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        UserDTO userDto = new UserDTO(user);

        response.status(200);
        sendJson(response, userDto);
    }
}