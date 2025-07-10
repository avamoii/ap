package org.example.actions.restaurant;

import com.google.gson.Gson;
import org.example.exception.ForbiddenException;
import org.example.exception.NotFoundException;
import org.example.model.Menu;
import org.example.repository.MenuRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Action to handle the DELETE /restaurants/{id}/menu/{title}/{item_id} request.
 * This action removes the association between a food item and a menu, but does not delete the food item itself.
 */
public class RemoveFoodItemFromMenuAction implements Route {

    private final Gson gson;
    private final MenuRepository menuRepository;

    public RemoveFoodItemFromMenuAction(Gson gson, MenuRepository menuRepository) {
        this.gson = gson;
        this.menuRepository = menuRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long restaurantId = Long.parseLong(request.params(":id"));
        String menuTitle = request.params(":title");
        Long itemIdToRemove = Long.parseLong(request.params(":item_id"));
        Long ownerIdFromToken = request.attribute("userId");

       //404
        Menu menu = menuRepository.findByRestaurantIdAndTitle(restaurantId, menuTitle)
                .orElseThrow(() -> new NotFoundException("Menu with title '" + menuTitle + "' not found in this restaurant."));

        //403
        if (!menu.getRestaurant().getOwner().getId().equals(ownerIdFromToken)) {
            throw new ForbiddenException("Access denied. You are not the owner of this restaurant.");
        }

        // 3. Remove the food item from the menu's list of items.
        // The removeIf method returns true if the list was changed.
        boolean removed = menu.getFoodItems().removeIf(foodItem -> foodItem.getId().equals(itemIdToRemove));

        // 4. If the item was not in the menu to begin with, throw a 404.
        if (!removed) {
            throw new NotFoundException("Food item with ID " + itemIdToRemove + " was not found in this menu.");
        }

        // 5. If the item was successfully removed from the list, update the menu in the database.
        menuRepository.update(menu);

        // 6. Return a success message.
        response.status(200);
        return gson.toJson(Map.of("message", "Item removed from restaurant menu successfully"));
    }
}
