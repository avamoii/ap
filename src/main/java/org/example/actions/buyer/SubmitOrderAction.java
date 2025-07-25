package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.OrderDTO;
import org.example.dto.SubmitOrderRequest;
import org.example.enums.CouponType;
import org.example.enums.OrderStatus;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.*;
import org.example.repository.*;
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

        // ۱. اعتبارسنجی ورودی
        if (orderRequest.getVendorId() == null || orderRequest.getDeliveryAddress() == null || orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new InvalidInputException("Missing required fields: vendor_id, delivery_address, and items are required.");
        }

        // ۲. پیدا کردن موجودیت‌های لازم
        User customer = userRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Customer not found."));
        Restaurant restaurant = restaurantRepository.findById(orderRequest.getVendorId()).orElseThrow(() -> new NotFoundException("Vendor not found."));

        BigDecimal rawPrice = BigDecimal.ZERO;
        List<FoodItem> foodItemsForOrder = new ArrayList<>();

        // ۳. بررسی موجودی و محاسبه قیمت اولیه
        for (var itemRequest : orderRequest.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new NotFoundException("Food item with ID " + itemRequest.getItemId() + " not found."));
            if (foodItem.getSupply() < itemRequest.getQuantity()) {
                throw new ResourceConflictException("Not enough supply for item: " + foodItem.getName());
            }
            // کاهش موجودی فقط در حافظه (در memory) انجام می‌شود
            foodItem.setSupply(foodItem.getSupply() - itemRequest.getQuantity());

            // ===> این خط حذف می‌شود تا از Cascade استفاده کنیم <===
            // foodItemRepository.update(foodItem);

            rawPrice = rawPrice.add(BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            foodItemsForOrder.add(foodItem);
        }

        BigDecimal finalPrice = new BigDecimal(rawPrice.toString());
        Coupon appliedCoupon = null;

        // ۴. منطق اعمال کوپن تخفیف
        if (orderRequest.getCouponId() != null) {
            appliedCoupon = couponRepository.findById(orderRequest.getCouponId())
                    .orElseThrow(() -> new NotFoundException("Coupon not found."));

            // اعتبارسنجی و اعمال تخفیف کوپن
            if (appliedCoupon.getMinPrice() != null && finalPrice.intValue() < appliedCoupon.getMinPrice()) {
                throw new InvalidInputException("Order total is less than the coupon's minimum price.");
            }
            if (appliedCoupon.getEndDate() != null && LocalDate.now().isAfter(appliedCoupon.getEndDate())) {
                throw new InvalidInputException("Coupon has expired.");
            }
            if (appliedCoupon.getUserCount() != null && appliedCoupon.getUserCount() <= 0) {
                throw new InvalidInputException("Coupon usage limit has been reached.");
            }

            if (appliedCoupon.getType() == CouponType.FIXED) {
                finalPrice = finalPrice.subtract(appliedCoupon.getValue());
            } else if (appliedCoupon.getType() == CouponType.PERCENT) {
                BigDecimal discountAmount = finalPrice.multiply(appliedCoupon.getValue()).divide(new BigDecimal(100));
                finalPrice = finalPrice.subtract(discountAmount);
            }

            if (appliedCoupon.getUserCount() != null) {
                appliedCoupon.setUserCount(appliedCoupon.getUserCount() - 1);
                couponRepository.update(appliedCoupon);
            }
        }

        // ۵. اضافه کردن هزینه‌های رستوران
        Integer taxFee = restaurant.getTaxFee() != null ? restaurant.getTaxFee() : 0;
        Integer additionalFee = restaurant.getAdditionalFee() != null ? restaurant.getAdditionalFee() : 0;
        finalPrice = finalPrice.add(BigDecimal.valueOf(taxFee));
        finalPrice = finalPrice.add(BigDecimal.valueOf(additionalFee));

        // TODO: هزینه پیک (courier_fee) باید در آینده محاسبه شود

        // ۶. ساختن و ذخیره سفارش جدید
        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setRestaurant(restaurant);
        newOrder.setItems(foodItemsForOrder); // <-- لیست آیتم‌های تغییر کرده به سفارش اضافه می‌شود
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

        // وقتی این خط اجرا می‌شود، هایبرنیت به صورت خودکار هم سفارش جدید را INSERT می‌کند
        // و هم تمام آیتم‌های غذایی تغییر کرده را UPDATE می‌کند، همه در یک تراکنش واحد.
        Order savedOrder = orderRepository.save(newOrder);

        // ۷. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(new OrderDTO(savedOrder));
    }
}
