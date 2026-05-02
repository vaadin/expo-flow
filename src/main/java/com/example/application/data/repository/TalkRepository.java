package com.example.application.data.repository;

import com.example.application.data.entity.Talk;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TalkRepository extends
        JpaRepository<Talk, Long>,
        JpaSpecificationExecutor<Talk> {

    @EntityGraph(attributePaths = "tracks")
    List<Talk> findAll();

    @EntityGraph(attributePaths = "tracks")
    List<Talk> findAll(Specification<Talk> spec);
}