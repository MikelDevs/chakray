package com.techtest.api;

import java.util.List;
import java.util.UUID;

public class User {
    public UUID id;
    public String email;
    public String name;
    public String phone;
    public String password;
    public String tax_id;
    public String created_at;
    public List<Address> addresses;

    public User(UUID id, String email, String name, String phone, String password, String tax_id,
            String created_at,
            List<Address> addresses) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.tax_id = tax_id;
        this.created_at = created_at;
        this.addresses = addresses;
    }
}
