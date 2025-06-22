package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.config.HibernateUtil;
import org.example.dto.RegisterRequest;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.example.util.JwtUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.*;

public class RegisterUserAction implements Route {

    private final Gson gson;
    public RegisterUserAction(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
        if (registerRequest.getPhoneNumber() == null || registerRequest.getPassword() == null ||
                registerRequest.getFirstName() == null || registerRequest.getLastName() == null ||
                registerRequest.getRole() == null) {
            response.status(400);
            return gson.toJson(Map.of("error", "Missing required fields"));
        }

        User registeredUser = null;
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User existingUser = session.createQuery("FROM User WHERE phoneNumber = :phoneNumber", User.class)
                    .setParameter("phoneNumber", registerRequest.getPhoneNumber())
                    .uniqueResult();

            if (existingUser != null) {
                response.status(409); // Conflict
                return gson.toJson(Map.of("error", "Phone number already exists"));
            }

            User newUser = new User();
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setPhoneNumber(registerRequest.getPhoneNumber());
            newUser.setRole(registerRequest.getRole());
            newUser.setAddress(registerRequest.getAddress());
            newUser.setPassword(registerRequest.getPassword());

            session.persist(newUser);
            transaction.commit();
            registeredUser = newUser;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            response.status(500);
            return gson.toJson(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }

        String token = JwtUtil.generateToken(registeredUser.getId(), registeredUser.getRole().toString());

        response.status(201);
        UserDTO userDto = new UserDTO(
                registeredUser.getId(),
                registeredUser.getFirstName(),
                registeredUser.getLastName(),
                registeredUser.getPhoneNumber(),
                registeredUser.getRole(),
                registeredUser.getAddress()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", userDto);
        responseData.put("token", token);
        return gson.toJson(responseData);
    }
}
