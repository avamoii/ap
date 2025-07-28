package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.config.HibernateUtil; // <-- ایمپورت جدید
import org.example.dto.OrderDTO;
import org.example.dto.SubmitOrderRequest;
import org.example.enums.CouponType;
import org.example.enums.OrderStatus;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.*;
import org.example.repository.*;
import org.hibernate.Session; // <-- ایمپورت جدید
import org.hibernate.Transaction; // <-- ایمپورت جدید
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmitOrderAction implements Route {
    private final Gson gson;
    // Repository ها دیگر در این کلاس مستقیم استفاده نمی‌شوند اما برای سازگاری نگه داشته شده‌اند
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;

    public SubmitOrderAction(Gson gson, UserRepository userRepository, RestaurantRepository restaurantRepository, FoodItemRepository foodItemRepository, OrderRepository orderRepository, CouponRepository couponRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
        this.orderRepository = orderRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long customerId = request.attribute("userId");
        SubmitOrderRequest orderRequest = gson.fromJson(request.body(), SubmitOrderRequest.class);

        if (orderRequest.getVendorId() == null || orderRequest.getDeliveryAddress() == null || orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new InvalidInputException("Missing required fields: vendor_id, delivery_address, and items are required.");
        }

        Transaction hibernateTransaction = null;
        Order savedOrder;

        // --- **تغییر اصلی: شروع یک تراکنش واحد برای کل عملیات** ---
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            hibernateTransaction = session.beginTransaction();

            User customer = session.get(User.class, customerId);
            if (customer == null) throw new NotFoundException("Customer not found.");

            Restaurant restaurant = session.get(Restaurant.class, orderRequest.getVendorId());
            if (restaurant == null) throw new NotFoundException("Vendor not found.");

            BigDecimal rawPrice = BigDecimal.ZERO;
            List<FoodItem> foodItemsForOrder = new ArrayList<>();

            // ۱. بررسی موجودی و کم کردن از آن (داخل تراکنش)
            for (var itemRequest : orderRequest.getItems()) {
                FoodItem foodItem = session.get(FoodItem.class, itemRequest.getItemId());
                if (foodItem == null) throw new NotFoundException("Food item with ID " + itemRequest.getItemId() + " not found.");

                if (foodItem.getSupply() < itemRequest.getQuantity()) {
                    throw new ResourceConflictException("Not enough supply for item: " + foodItem.getName());
                }
                foodItem.setSupply(foodItem.getSupply() - itemRequest.getQuantity());
                session.merge(foodItem); // تغییرات در حافظه Hibernate ثبت می‌شود

                rawPrice = rawPrice.add(BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                foodItemsForOrder.add(foodItem);
            }

            BigDecimal finalPrice = new BigDecimal(rawPrice.toString());
            Coupon appliedCoupon = null;

            // ۲. اعمال کوپن (داخل تراکنش)
            if (orderRequest.getCouponId() != null) {
                appliedCoupon = session.get(Coupon.class, orderRequest.getCouponId());
                if(appliedCoupon == null) throw new NotFoundException("Coupon not found.");
                // (منطق بررسی اعتبار کوپن)
                // ...
                if (appliedCoupon.getType() == CouponType.FIXED) {
                    finalPrice = finalPrice.subtract(appliedCoupon.getValue());
                } else if (appliedCoupon.getType() == CouponType.PERCENT) {
                    BigDecimal discountAmount = finalPrice.multiply(appliedCoupon.getValue()).divide(new BigDecimal(100));
                    finalPrice = finalPrice.subtract(discountAmount);
                }
                if (appliedCoupon.getUserCount() != null) {
                    appliedCoupon.setUserCount(appliedCoupon.getUserCount() - 1);
                    session.merge(appliedCoupon);
                }
            }

            Integer taxFee = restaurant.getTaxFee() != null ? restaurant.getTaxFee() : 0;
            Integer additionalFee = restaurant.getAdditionalFee() != null ? restaurant.getAdditionalFee() : 0;
            finalPrice = finalPrice.add(BigDecimal.valueOf(taxFee)).add(BigDecimal.valueOf(additionalFee));

            // ۳. ساخت و ذخیره سفارش (داخل تراکنش)
            Order newOrder = new Order();
            newOrder.setCustomer(customer);
            newOrder.setRestaurant(restaurant);
            newOrder.setItems(foodItemsForOrder);
            newOrder.setDeliveryAddress(orderRequest.getDeliveryAddress());
            newOrder.setStatus(OrderStatus.SUBMITTED);
            newOrder.setRawPrice(rawPrice.intValue());
            newOrder.setTaxFee(taxFee);
            newOrder.setAdditionalFee(additionalFee);
            newOrder.setPayPrice(finalPrice.intValue() > 0 ? finalPrice.intValue() : 0);
            newOrder.setCreatedAt(LocalDateTime.now());
            if (appliedCoupon != null) {
                newOrder.setCoupon(appliedCoupon);
            }

            session.persist(newOrder);
            savedOrder = newOrder;

            // ۴. اگر همه چیز موفق بود، کل تغییرات را با هم در دیتابیس ذخیره کن
            hibernateTransaction.commit();

        } catch (Exception e) {
            // ۵. اگر هر خطایی در هر مرحله‌ای رخ داد، کل عملیات را لغو کن
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            // ارور را به فرانت‌اند ارسال کن تا نمایش داده شود
            throw e;
        }

        response.status(200);
        // از آبجکت ذخیره شده برای ساخت DTO استفاده می‌کنیم
        return gson.toJson(new OrderDTO(savedOrder));
    }
}