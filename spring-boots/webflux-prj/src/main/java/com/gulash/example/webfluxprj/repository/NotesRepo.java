package com.gulash.example.webfluxprj.repository;

import com.gulash.example.webfluxprj.model.Notes;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface NotesRepo extends /*todo реактивный репозиторий*/ReactiveCrudRepository<Notes, Long> {
    Flux<Notes> findByPersonId(Long personId);
}
