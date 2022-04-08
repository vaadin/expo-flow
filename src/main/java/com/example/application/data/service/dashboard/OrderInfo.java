package com.example.application.data.service.dashboard;

import java.util.List;

public class OrderInfo {

    private String city;
    private List<Integer> values;

    public OrderInfo(String city, List<Integer> values) {
        this.city = city;
        this.values = values;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }
}
