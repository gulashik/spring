package com.gulash.example.assertjunitdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Spring!";
    }
}