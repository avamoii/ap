// ==================== USER MANAGEMENT CONTROLLERS ====================
package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.UserDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ListUsersAdminController extends BaseController {
    private final UserRepository userRepository;

    public ListUsersAdminController(Gson gson, UserRepository userRepository) {
        super(gson);
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String userRole = getCurrentUserRole();

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Fetch all users from the repository.
        List<User> users = userRepository.findAll();

        // 3. Convert the list of User entities to a list of UserDTOs to avoid exposing sensitive data.
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        sendJson(response, userDTOs);
    }
}
