package com.sprboot.testcontext.jdbcpostgre.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Data
public class Author {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final Integer yearOfBirth;
    private final Integer distinguished;
}
