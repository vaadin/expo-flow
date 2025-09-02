package com.example.application.data.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
    @NotNull
    private String shirtSize;

    private Integer points;

    public TShirtOrder() {
    }

    public TShirtOrder(Integer points) {
        this.points = points;
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

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
