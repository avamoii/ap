// File: src/main/java/org/example/repository/TransactionRepository.java
package org.example.repository;

import org.example.model.Transaction;
import java.util.List;

public interface TransactionRepository {
    List<Transaction> findByUserId(Long userId);
    Transaction save(Transaction transaction);
}
