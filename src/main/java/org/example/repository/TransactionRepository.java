// File: src/main/java/org/example/repository/TransactionRepository.java
package org.example.repository;

import org.example.model.Transaction;
import java.util.*;

public interface TransactionRepository {
    List<Transaction> findByUserId(Long userId);
    Transaction save(Transaction transaction);
    List<Transaction> findAllWithFilters(Map<String, String[]> filters);
}
