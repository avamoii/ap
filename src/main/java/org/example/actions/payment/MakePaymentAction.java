package org.example.actions.payment;

import com.google.gson.Gson;
import org.example.dto.PaymentRequest;
import org.example.dto.TransactionDTO;
import org.example.enums.OrderStatus;
import org.example.enums.TransactionType;
import org.example.exception.ForbiddenException;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.Order;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.repository.OrderRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;

public class MakePaymentAction implements Route {

    private final Gson gson;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public MakePaymentAction(Gson gson, OrderRepository orderRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.gson = gson;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userIdFromToken = request.attribute("userId");
        PaymentRequest paymentRequest = gson.fromJson(request.body(), PaymentRequest.class);

        // ۱. اعتبارسنجی ورودی (برای خطای 400)
        if (paymentRequest.getOrderId() == null || paymentRequest.getMethod() == null) {
            throw new InvalidInputException("Missing required fields: order_id and method are required.");
        }

        // ۲. پیدا کردن سفارش (برای خطای 404)
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        // ۳. بررسی مالکیت سفارش (برای خطای 403)
        if (!order.getCustomer().getId().equals(userIdFromToken)) {
            throw new ForbiddenException("Access denied. You can only pay for your own orders.");
        }

        // ۴. بررسی وضعیت سفارش (برای خطای 409)
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new ResourceConflictException("This order cannot be paid for. It might be already paid or cancelled.");
        }

        // ۵. منطق پرداخت
        switch (paymentRequest.getMethod()) {
            case WALLET:
                User customer = userRepository.findById(userIdFromToken)
                        .orElseThrow(() -> new NotFoundException("Customer not found.")); // Should not happen

                if (customer.getWalletBalance() < order.getPayPrice()) {
                    // --- تغییر کلیدی ---
                    // دیگر سفارش را کنسل نمی‌کنیم، فقط یک پیام خطا برمی‌گردانیم
                    throw new InvalidInputException("Insufficient wallet balance. Please top up your wallet and try again.");
                }

                customer.setWalletBalance(customer.getWalletBalance() - order.getPayPrice());
                userRepository.update(customer);
                break;
            case ONLINE:
                // For this simulation, we assume the online payment is always successful.
                break;
        }

        // ۶. به‌روزرسانی وضعیت سفارش
        order.setStatus(OrderStatus.WAITING_VENDOR);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(order);

        // ۷. ثبت تراکنش پرداخت
        Transaction paymentTransaction = new Transaction();
        paymentTransaction.setUser(order.getCustomer());
        paymentTransaction.setOrder(order);
        paymentTransaction.setType(TransactionType.PAYMENT);
        paymentTransaction.setAmount(-order.getPayPrice()); // Amount is negative as it's a debit
        paymentTransaction.setCreatedAt(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(paymentTransaction);

        // ۸. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(new TransactionDTO(savedTransaction));
    }
}
