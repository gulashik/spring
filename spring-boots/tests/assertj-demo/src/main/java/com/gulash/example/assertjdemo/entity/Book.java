package com.gulash.example.assertjdemo.entity;

public record Book(
    Long id,
    String title,
    User author,
    String isbn,
    Integer attr1,
    Integer attr2
){}
