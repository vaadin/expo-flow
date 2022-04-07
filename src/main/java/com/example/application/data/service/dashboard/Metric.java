package com.example.application.data.service.dashboard;

import java.text.Format;

public class Metric {
    private String name;
    private Double value;
    private String unit;
    private Double change;
    private Format formatter;


    public Metric(String name, Double value, String unit, Format formatter) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.change = 0.0;
        this.formatter = formatter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Format getFormatter() {
        return formatter;
    }

    public void setFormatter(Format formatter) {
        this.formatter = formatter;
    }
}
