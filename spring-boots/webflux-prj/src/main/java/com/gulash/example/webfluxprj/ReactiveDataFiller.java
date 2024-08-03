package com.gulash.example.webfluxprj;

import com.gulash.example.webfluxprj.model.Notes;
import com.gulash.example.webfluxprj.model.Person;
import com.gulash.example.webfluxprj.repository.PersonRepoCustom;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import reactor.core.scheduler.Scheduler;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.gulash.example.webfluxprj.repository.PersonRepo;
import com.gulash.example.webfluxprj.repository.NotesRepo;

@RequiredArgsConstructor
@Component
public class ReactiveDataFiller implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveDataFiller.class);

    private final PersonRepo personRepo;
    private final NotesRepo notesRepo;
    private final PersonRepoCustom personRepoCustom;
    private final Scheduler workerPool;
//    private final Flyway flyway;

    @Override
    public void run(ApplicationArguments args) {
//        flyway.migrate();

        // todo реактиное заполнение
        personRepo.saveAll(
                Arrays.asList(
                    new Person("Pushkin", 22),
                    new Person("Lermontov", 22),
                    new Person("Tolstoy", 60)
                )
            ).publishOn(workerPool)
            .subscribe(savedPerson -> {
                logger.info("saved person:{}", savedPerson);
                notesRepo.saveAll(Arrays.asList(
                        new Notes(null, "txt_1_" + savedPerson.getId(), savedPerson.getId()),
                        new Notes(null, "txt_2_" + savedPerson.getId(), savedPerson.getId())))
                    .publishOn(workerPool)
                    .subscribe(savedNotes -> logger.info("saved notes:{}", savedNotes));
            });

        personRepoCustom.findAll()
            .publishOn(workerPool)
            .subscribe(personDto -> logger.info("personDto:{}", personDto));
    }
}