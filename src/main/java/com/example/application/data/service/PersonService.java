package com.example.application.data.service;

import com.example.application.data.entity.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public Optional<Person> get(UUID id) {
        return repository.findById(id);
    }

    public Person update(Person entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Stream<Person> list(Pageable pageable) {
        return repository.findAll(pageable).stream();
    }

    public int count() {
        return (int) repository.count();
    }

    public List<Person> findAll() {
        return repository.findAll();
    }

}
