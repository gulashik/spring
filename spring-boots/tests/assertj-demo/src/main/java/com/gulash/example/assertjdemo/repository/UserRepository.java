package com.gulash.example.assertjdemo.repository;

import com.gulash.example.assertjdemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
