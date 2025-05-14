package com.example.application.service;

import com.example.application.data.entity.Person;
import com.example.application.data.repository.PersonRepository;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@BrowserCallable
@AnonymousAllowed
@Service
public class PersonService extends CrudRepositoryService<Person, UUID, PersonRepository> {

    public List<Person> findAll() {
        return getRepository().findAll();
    }

    public List<Person> unfilteredList(Pageable pageable) {
        return super.list(pageable, null);
    }
}
