package com.gulash.example.webfluxprj.controller;

import com.gulash.example.webfluxprj.model.Person;
import com.gulash.example.webfluxprj.model.PersonDto;
import com.gulash.example.webfluxprj.model.Notes;
import com.gulash.example.webfluxprj.repository.NotesRepo;
import com.gulash.example.webfluxprj.repository.PersonRepo;
import com.gulash.example.webfluxprj.repository.PersonRepoCustom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class PersonController {

    private final PersonRepo personRepo;
    private final NotesRepo notesRepo;

    private final PersonRepoCustom personRepoCustom;

    public PersonController(PersonRepo personRepo, NotesRepo notesRepo, PersonRepoCustom personRepoCustom) {
        this.personRepo = personRepo;
        this.notesRepo = notesRepo;
        this.personRepoCustom = personRepoCustom;
    }

    @GetMapping("/person")
    public Flux<PersonDto> all() {
        return personRepoCustom.findAll();
    }

    @GetMapping("/person/{id}")
    public Mono<ResponseEntity<PersonDto>> byId(@PathVariable("id") Long id) {
        return personRepo.findById(id)
            .flatMap(person -> notesRepo.findByPersonId(person.getId()).map(Notes::getNoteText).collectList()
                .map(notes -> toDto(person, notes)))
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.fromCallable(() -> ResponseEntity.notFound().build()));
    }

    @PostMapping("/person")
    public Mono<Person> save(@RequestBody Mono<Person> dto) {
        return personRepo.save(dto);
    }

    @GetMapping("/person/find")
    public Flux<Person> byName(@RequestParam("name") String name) {
        return personRepo.findAllByLastName(name);
    }

    private PersonDto toDto(Person person, List<String> notes) {
        return new PersonDto(String.valueOf(person.getId()), person.getLastName(), person.getAge(), notes);
    }
}