
package com.example.application.data.repository;

import com.example.application.data.entity.TShirtOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Empty JpaRepository is enough for a simple crud.
 */
public interface TShirtOrderRepository extends JpaRepository<TShirtOrder, Long> {
    
}
