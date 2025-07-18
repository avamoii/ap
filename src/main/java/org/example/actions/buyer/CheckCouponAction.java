package org.example.actions.buyer;

import com.google.gson.Gson;
import org.example.dto.CouponDTO;
import org.example.exception.InvalidInputException;
import org.example.exception.NotFoundException;
import org.example.model.Coupon;
import org.example.repository.CouponRepository;
import spark.Request;
import spark.Response;
import spark.Route;

public class CheckCouponAction implements Route {

    private final Gson gson;
    private final CouponRepository couponRepository;

    public CheckCouponAction(Gson gson, CouponRepository couponRepository) {
        this.gson = gson;
        this.couponRepository = couponRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        // ۱. خواندن پارامتر کوئری (برای خطای 400)
        String couponCode = request.queryParams("coupon_code");
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new InvalidInputException("Query parameter 'coupon_code' is required.");
        }

        // ۲. پیدا کردن کوپن (برای خطای 404)
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new NotFoundException("Coupon with code '" + couponCode + "' not found."));

        // TODO: در آینده می‌توانید منطق بیشتری اضافه کنید (مثلاً چک کردن تاریخ انقضا یا تعداد استفاده)

        // ۳. ارسال پاسخ موفقیت‌آمیز
        response.status(200);
        return gson.toJson(new CouponDTO(coupon));
    }
}
