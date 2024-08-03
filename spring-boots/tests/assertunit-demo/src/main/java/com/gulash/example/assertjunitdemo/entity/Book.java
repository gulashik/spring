package com.gulash.example.assertjunitdemo.entity;

public record Book(
    Long id,
    String title,
    User author,
    String isbn,
    Integer attr1,
    Integer attr2
){}
