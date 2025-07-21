package org.example.model;

import jakarta.persistence.*;
import org.example.enums.UserRole;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    private String address;
    @Column(unique = true)
    private String email;
    @Lob
    private String profileImageBase64;
    @Embedded
    private BankInfo bankInfo;

    // A user can have many favorite restaurants.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_favorite_restaurants", // Name of the intermediate table
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer walletBalance = 0;
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public BankInfo getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfo bankInfo) { this.bankInfo = bankInfo; }
    public List<Restaurant> getFavoriteRestaurants() { return favoriteRestaurants; }
    public void setFavoriteRestaurants(List<Restaurant> favoriteRestaurants) { this.favoriteRestaurants = favoriteRestaurants; }
    public Integer getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Integer walletBalance) { this.walletBalance = walletBalance; }
}
