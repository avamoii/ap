package org.example.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String name, String phone, String email, String address) {
        super(name, phone, email, address);
    }
}


