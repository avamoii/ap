package org.example.model;

import jakarta.persistence.Entity;

@Entity
public class Seller extends User {
    public Seller() { super(); }

    public Seller(String name, String phone, String email, String address) {
        super(name, phone, email, address);
    }
}
