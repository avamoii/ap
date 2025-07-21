package org.example.actions.transaction;

import com.google.gson.Gson;
import org.example.dto.TransactionDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class GetTransactionHistoryAction implements Route {

    private final Gson gson;
    private final TransactionRepository transactionRepository;

    public GetTransactionHistoryAction(Gson gson, TransactionRepository transactionRepository) {
        this.gson = gson;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");

        // ۱. دریافت تاریخچه تراکنش‌ها از ریپازیتوری
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // ۲. تبدیل لیست موجودیت‌ها به لیست DTOها
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        // ۳. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(transactionDTOs);
    }
}
