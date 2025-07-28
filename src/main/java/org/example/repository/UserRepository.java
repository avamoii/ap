package org.example.repository;

import org.example.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    User update(User user);
    List<User> findAll();
    void processWalletPayment(Long userId, Integer amount, Long orderId);
    void processWalletDeposit(Long userId, Integer amount);
}