package org.example.model;

import jakarta.persistence.Entity;

    @Entity
    public class Customer extends User {

        public Customer() {
            super();
        }

        public Customer(String name, String phone, String email, String address) {
            super(name, phone, email, address);
        }

    }


