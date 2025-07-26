package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.UserDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /admin/users request.
 * Fetches a list of all users, accessible only by an admin.
 */
public class ListUsersAdminAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public ListUsersAdminAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Fetch all users from the repository.
        List<User> users = userRepository.findAll();

        // 3. Convert the list of User entities to a list of UserDTOs to avoid exposing sensitive data.
        // --- تغییر اصلی اینجاست ---
        // از سازنده جدید UserDTO استفاده می‌کنیم
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new) // به جای new UserDTO(user.getId(), ...)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        return gson.toJson(userDTOs);
    }
}
