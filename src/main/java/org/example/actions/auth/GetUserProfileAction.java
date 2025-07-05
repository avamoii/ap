// File: src/main/java/org/example/actions/auth/GetUserProfileAction.java
package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.dto.UserDTO;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

public class GetUserProfileAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public GetUserProfileAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        // ۱. دریافت userId که توسط فیلتر JWT به درخواست اضافه شده است
        Long userId = request.attribute("userId");

        // ۲. استفاده از ریپازیتوری برای پیدا کردن کاربر با ID او
        Optional<User> userOptional = userRepository.findById(userId);

        // ۳. اگر کاربر پیدا نشد (مثلاً بعد از صدور توکن حذف شده باشد)، خطای 404 پرتاب کن
        User user = userOptional.orElseThrow(() -> new NotFoundException("Resource not found."));

        // ۴. اگر پیدا شد، موجودیت User را به یک UserDTO تبدیل کن تا به کلاینت ارسال شود
        //    این کار تضمین می‌کند که داده‌های حساس مثل رمز عبور به بیرون درز نمی‌کند
        UserDTO userDto = new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getAddress()
                // می‌توانید فیلدهای دیگر مثل ایمیل و اطلاعات بانک را هم به UserDTO اضافه کنید تا در پاسخ برگردانده شوند
        );

        response.status(200); // OK
        return gson.toJson(userDto);
    }
}
