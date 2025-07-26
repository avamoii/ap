package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.dto.UserDTO;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

public class GetUserProfileAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public GetUserProfileAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        Long userId = request.attribute("userId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // --- تغییر اصلی اینجاست ---
        // از سازنده جدید UserDTO استفاده می‌کنیم که تمام اطلاعات،
        // از جمله موجودی کیف پول را شامل می‌شود.
        UserDTO userDto = new UserDTO(user);

        response.status(200); // OK
        return gson.toJson(userDto);
    }
}
