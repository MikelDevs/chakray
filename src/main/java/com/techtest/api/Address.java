package com.techtest.api;

public class Address {
    public int id;
    public String name;
    public String street;
    public String country_code;

    public Address(int id, String name, String street, String country_code) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.country_code = country_code;
    }
}
