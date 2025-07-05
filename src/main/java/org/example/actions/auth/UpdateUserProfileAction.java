package org.example.actions.auth;

import com.google.gson.Gson;
import org.example.dto.UpdateProfileRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ResourceConflictException;
import org.example.model.BankInfo;
import org.example.model.User;
import org.example.repository.UserRepository;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;
import java.util.Optional;

public class UpdateUserProfileAction implements Route {

    private final Gson gson;
    private final UserRepository userRepository;

    public UpdateUserProfileAction(Gson gson, UserRepository userRepository) {
        this.gson = gson;
        this.userRepository = userRepository;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Long userId = request.attribute("userId");
        UpdateProfileRequest updateRequest = gson.fromJson(request.body(), UpdateProfileRequest.class);

       //404
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Recourse not found."));

        //409
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().equals(currentUser.getPhoneNumber())) {
            Optional<User> userWithSamePhone = userRepository.findByPhoneNumber(updateRequest.getPhone());
            if (userWithSamePhone.isPresent()) {
                throw new ResourceConflictException("This phone number is already taken by another user.");
            }
            currentUser.setPhoneNumber(updateRequest.getPhone());
        }

        //409
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(currentUser.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(updateRequest.getEmail());
            if (userWithSameEmail.isPresent()) {
                throw new ResourceConflictException("This email is already taken by another user.");
            }
            currentUser.setEmail(updateRequest.getEmail());
        }


        if (updateRequest.getFullName() != null) {
            String[] names = updateRequest.getFullName().trim().split("\\s+", 2);
            currentUser.setFirstName(names.length > 0 ? names[0] : "");
            currentUser.setLastName(names.length > 1 ? names[1] : "");
        }
        if (updateRequest.getAddress() != null) {
            currentUser.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getProfileImageBase64() != null) {
            currentUser.setProfileImageBase64(updateRequest.getProfileImageBase64());
        }
        if (updateRequest.getBankInfo() != null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBankName(updateRequest.getBankInfo().getBankName());
            bankInfo.setAccountNumber(updateRequest.getBankInfo().getAccountNumber());
            currentUser.setBankInfo(bankInfo);
        }

        // ۵. کاربر به‌روز شده را در دیتابیس ذخیره کن
        userRepository.update(currentUser);

        // ۶. پاسخ موفقیت‌آمیز را برگردان
        response.status(200);
        return gson.toJson(Map.of("message", "Profile updated successfully"));
    }
}
