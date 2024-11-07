package com.example.application.service;

import com.example.application.data.entity.TShirtOrder;
import com.example.application.data.repository.TShirtOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * A service class for the UI to access backend services.
 */
@Service
public class TShirtService {
    
    @Autowired
    private TShirtOrderRepository repository;

    public List<String> getSizes() {
        return Arrays.asList("Small", "Medium", "Large", "Extra Large", "XXL");
    }

    public void placeOrder(TShirtOrder order) throws IllegalArgumentException {
        repository.save(order);
    }

}
