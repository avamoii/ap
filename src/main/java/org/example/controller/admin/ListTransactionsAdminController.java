// ==================== TRANSACTION MANAGEMENT CONTROLLER ====================
package org.example.controller.admin;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.TransactionDTO;
import org.example.enums.UserRole;
import org.example.exception.ForbiddenException;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListTransactionsAdminController extends BaseController {
    private final TransactionRepository transactionRepository;

    public ListTransactionsAdminController(Gson gson, TransactionRepository transactionRepository) {
        super(gson);
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String userRole = getCurrentUserRole();

        // 1. Check if the user has the ADMIN role (handles 403 Forbidden)
        if (!UserRole.ADMIN.toString().equals(userRole)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // Get the original query parameters
        Map<String, String> queryParams = request.getQueryParams();

        // **FIX:** Convert the Map<String, String> to a Map<String, String[]>
        Map<String, String[]> filters = queryParams.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new String[]{entry.getValue()}
                ));

        // 2. Fetch all transactions using the correctly typed filters map
        List<Transaction> transactions = transactionRepository.findAllWithFilters(filters);

        // 3. Convert the list of Transaction entities to a list of TransactionDTOs
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        // 4. Send the successful response
        response.status(200);
        sendJson(response, transactionDTOs);
    }
}