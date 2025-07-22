package org.example.actions.admin;

import com.google.gson.Gson;
import org.example.dto.TransactionDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action to handle the GET /admin/transactions request.
 * Fetches a list of all transactions with optional filters, accessible only by an admin.
 */
public class ListTransactionsAdminAction implements Route {

    private final Gson gson;
    private final TransactionRepository transactionRepository;

    public ListTransactionsAdminAction(Gson gson, TransactionRepository transactionRepository) {
        this.gson = gson;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        String userRole = request.attribute("userRole");

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // 2. Fetch all transactions from the repository, passing the query parameters as filters.
        List<Transaction> transactions = transactionRepository.findAllWithFilters(request.queryMap().toMap());

        // 3. Convert the list of Transaction entities to a list of TransactionDTOs.
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response.
        response.status(200);
        return gson.toJson(transactionDTOs);
    }
}
