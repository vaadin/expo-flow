package com.example.application.data.repository;

import com.example.application.data.entity.Person;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonRepository extends JpaRepository<Person, UUID>,
        JpaSpecificationExecutor<Person> {

}