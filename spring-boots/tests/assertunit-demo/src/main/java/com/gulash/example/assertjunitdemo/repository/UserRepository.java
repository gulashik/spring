package com.gulash.example.assertjunitdemo.repository;

import com.gulash.example.assertjunitdemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
