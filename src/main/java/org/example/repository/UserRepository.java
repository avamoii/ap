package org.example.repository;

import org.example.model.User;
import java.util.*;
public interface UserRepository  {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    User save(User user);
}