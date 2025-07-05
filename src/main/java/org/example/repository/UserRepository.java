package org.example.repository;

import org.example.model.User;
import java.util.Optional;

/**
 * Interface for data access operations on User entities.
 * This defines the contract for our repository.
 */
public interface UserRepository {

    /**
     * Finds a user by their phone number.
     * @param phoneNumber The phone number to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByEmail(String email);

    /**
     * Saves a new user or updates an existing one.
     * @param user The user entity to save.
     * @return The saved user entity (with the generated ID).
     */
    User save(User user);

    /**
     * Finds a user by their unique ID (Primary Key).
     * @param id The ID of the user to find.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findById(Long id);
}
