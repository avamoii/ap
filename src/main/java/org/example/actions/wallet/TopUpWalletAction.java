package org.example.actions.wallet;

import com.google.gson.Gson;
import org.example.dto.TopUpWalletRequest;
import org.example.exception.InvalidInputException;
import org.example.repository.UserRepository;
import spark.Request;
import org.example.repository.TransactionRepository;
import spark.Response;
import spark.Route;

import java.util.Map;

public class TopUpWalletAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;
    // TransactionRepository is no longer needed here as the logic is moved to UserRepository
    // but we keep it in the constructor for dependency injection consistency.
    private final TransactionRepository transactionRepository;

    public TopUpWalletAction(Gson gson, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");
        TopUpWalletRequest topUpRequest = gson.fromJson(request.body(), TopUpWalletRequest.class);

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
            return gson.toJson(Map.of("message", "Wallet topped up successfully"));

        } catch (Exception e) {
            // 4. If anything goes wrong inside the transaction, catch the error and respond
            response.status(500);
            return gson.toJson(Map.of("error", e.getMessage()));
        }
    }
}