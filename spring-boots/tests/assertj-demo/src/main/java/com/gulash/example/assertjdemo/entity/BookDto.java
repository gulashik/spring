package com.gulash.example.assertjdemo.entity;

public record BookDto(
    Long id,
    String title,
    Integer authorId
){}
