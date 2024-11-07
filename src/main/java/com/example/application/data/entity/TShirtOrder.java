package com.example.application.data.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * Created by mstahv
 */
@Entity
public class TShirtOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty
    @Email
    private String email  = "";
    @NotEmpty
    private String name = "";
    @NotEmpty
    private String shirtSize;

    public TShirtOrder() {
    }

    public TShirtOrder(String name, String email, String shirtSize) {
        this.name = name;
        this.email = email;
        this.shirtSize = shirtSize;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShirtSize() {
        return shirtSize;
    }

    public void setShirtSize(String shirtSize) {
        this.shirtSize = shirtSize;
    }
}
