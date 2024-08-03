package com.gulash.example.webfluxprj.repository;

import com.gulash.example.webfluxprj.model.Person;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface  PersonRepo extends ReactiveCrudRepository<Person, Long> {

    @NotNull
    Mono<Person> findById(@NotNull Long id);

    Mono<Person> save(Mono<Person> person);

    Flux<Person> findAllByLastName(String lastName);

    Flux<Person> findAllByAge(int age);
}