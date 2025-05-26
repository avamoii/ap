package org.example.model;


import jakarta.persistence.Entity;

@Entity
public class Delivery extends User {
    public Delivery() { super(); }

    public Delivery(String name, String phone, String email, String address) {
        super(name, phone, email, address);
    }
}
