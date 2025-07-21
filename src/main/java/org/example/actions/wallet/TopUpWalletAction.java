package org.example.actions.wallet;

import com.google.gson.Gson;
import org.example.dto.TopUpWalletRequest;
import org.example.enums.TransactionType;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;
import java.util.Map;

public class TopUpWalletAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;
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

        // ۱. اعتبارسنجی ورودی (برای خطای 400)
        if (topUpRequest.getAmount() == null || topUpRequest.getAmount() <= 0) {
            throw new InvalidInputException("Invalid amount. Amount must be a positive number.");
        }

        // ۲. پیدا کردن کاربر
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // ۳. افزایش موجودی کیف پول
        user.setWalletBalance(user.getWalletBalance() + topUpRequest.getAmount());
        userRepository.update(user);

        // ۴. ثبت تراکنش واریز
        Transaction depositTransaction = new Transaction();
        depositTransaction.setUser(user);
        depositTransaction.setType(TransactionType.DEPOSIT);
        depositTransaction.setAmount(topUpRequest.getAmount());
        depositTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(depositTransaction);

        // ۵. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(Map.of("message", "Wallet topped up successfully"));
    }
}
