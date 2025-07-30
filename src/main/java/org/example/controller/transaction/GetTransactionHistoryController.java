// ==================== TRANSACTION CONTROLLER ====================
package org.example.controller.transaction;

import com.google.gson.Gson;
import org.example.controller.BaseController;
import org.example.core.HttpRequest;
import org.example.core.HttpResponse;
import org.example.dto.TransactionDTO;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetTransactionHistoryController extends BaseController {
    private final TransactionRepository transactionRepository;

    public GetTransactionHistoryController(Gson gson, TransactionRepository transactionRepository) {
        super(gson);
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Long userId = getCurrentUserId();

        // ۱. دریافت تاریخچه تراکنش‌ها از ریپازیتوری
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // ۲. تبدیل لیست موجودیت‌ها به لیست DTOها
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        // ۳. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        sendJson(response, transactionDTOs);
    }
}