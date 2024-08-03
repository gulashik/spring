package com.gulash.example.assertjunitdemo.entity;

public record BookDto(
    Long id,
    String title,
    Integer authorId
){}
