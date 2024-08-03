package com.gulash.example.webfluxprj.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@ToString
@Table("notes")
public class Notes {
    @Id
    private final Long id;

    private final String noteText;

    private final Long personId;

    @PersistenceCreator
    public Notes(Long id, String noteText, Long personId) {
        this.id = id;
        this.noteText = noteText;
        this.personId = personId;
    }
}

