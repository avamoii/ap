// ==================== WALLET CONTROLLER ====================
package org.example.controller.wallet;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.TopUpWalletRequest;
import org.example.exception.InvalidInputException;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;

import java.util.Map;

public class TopUpWalletController extends BaseController {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TopUpWalletController(Gson gson, UserRepository userRepository, TransactionRepository transactionRepository) {
        super(gson);
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userId = getCurrentUserId();
        TopUpWalletRequest topUpRequest = gson.fromJson(request.getBody(), TopUpWalletRequest.class);

        // 1. Validate the input amount
        if (topUpRequest.getAmount() == null || topUpRequest.getAmount() <= 0) {
            throw new InvalidInputException("Invalid amount. Amount must be a positive number.");
        }

        try {
            // --- **THE MAIN FIX IS HERE** ---
            // 2. Call the new, safe method that handles everything in one transaction.
            // This single line replaces the separate steps of finding, updating, and saving.
            userRepository.processWalletDeposit(userId, topUpRequest.getAmount());

            // 3. Return a success response
            response.status(200);
            sendJson(response, Map.of("message", "Wallet topped up successfully"));

        } catch (Exception e) {
            // 4. If anything goes wrong inside the transaction, catch the error and respond
            response.status(500);
            sendJson(response, Map.of("error", e.getMessage()));
        }
    }
}