package com.sprboot.mockkexample.kotlinmockkexample.service

import com.sprboot.mockkexample.kotlinmockkexample.domain.User
import org.springframework.stereotype.Service

@Service
class UserService {
    fun getUserById(id: Long): User {
        // Здесь можно реализовать логику для получения пользователя из базы данных
        return User(id, "User$id")
    }
}