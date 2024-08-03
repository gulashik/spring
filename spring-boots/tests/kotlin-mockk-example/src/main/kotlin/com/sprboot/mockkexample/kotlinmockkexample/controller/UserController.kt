package com.sprboot.mockkexample.kotlinmockkexample.controller

import com.sprboot.mockkexample.kotlinmockkexample.calc.Calculator
import com.sprboot.mockkexample.kotlinmockkexample.domain.User
import com.sprboot.mockkexample.kotlinmockkexample.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
    private val calculator: Calculator,
    ) {

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): User {
        calculator.add(id.toInt(),id.toInt())
        return userService.getUserById(id)
    }
}