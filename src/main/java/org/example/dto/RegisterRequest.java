package org.example.dto;

public class RegisterRequest {

    private String full_name;
    private String phone;
    private String email;
    private String password;
    private String role;
    private String address;
    private String profileImageBase64;
    private BankInfoDTO bank_info;

    // Getters and Setters


    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public BankInfoDTO getBank_info() { return bank_info; }
    public void setBank_info(BankInfoDTO bank_info) { this.bank_info = bank_info; }



    // nested DTO
    public static class BankInfoDTO {
        private String bank_name;
        private String account_number;

        public String getBank_name() { return bank_name; }
        public void setBank_name(String bank_name) { this.bank_name = bank_name; }

        public String getAccount_number() { return account_number; }
        public void setAccount_number(String account_number) { this.account_number = account_number; }
    }
}
