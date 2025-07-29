// 7. CREATE MENU CONTROLLER
package org.example.controller.restaurant;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.CreateMenuRequest;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Menu;
import org.example.model.Restaurant;
import org.example.repository.MenuRepository;
import org.example.repository.RestaurantRepository;

import java.util.Map;

public class CreateMenuController extends BaseController {
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;

    public CreateMenuController(Gson gson, RestaurantRepository restaurantRepository, MenuRepository menuRepository) {
        super(gson);
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long restaurantId = Long.parseLong(request.getPathParam("id"));
        Long ownerIdFromToken = getCurrentUserId();
        String userRole = getCurrentUserRole();

        // Check user role (403)
        if (!UserRole.SELLER.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Only sellers can create menus.");
        }

        // Parse and validate request (400)
        CreateMenuRequest createRequest = gson.fromJson(request.getBody(), CreateMenuRequest.class);
        if (createRequest.getTitle() == null || createRequest.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Missing required field: title is required.");
        }

        // Find restaurant (404)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found."));

        // Check ownership (403)
        if (!restaurant.getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // Check for duplicate menu title (409)
        menuRepository.findByRestaurantIdAndTitle(restaurantId, createRequest.getTitle())
                .ifPresent(menu -> {
                    throw new ResourceConflictException("A menu with this title already exists for this restaurant.");
                });

        // Create and save new menu
        Menu newMenu = new Menu();
        newMenu.setTitle(createRequest.getTitle());
        newMenu.setRestaurant(restaurant);
        Menu savedMenu = menuRepository.save(newMenu);

        // Send success response
        response.status(200);
        sendJson(response, Map.of("title", savedMenu.getTitle()));
    }
}
