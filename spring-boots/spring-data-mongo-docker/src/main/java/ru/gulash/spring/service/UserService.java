package ru.gulash.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gulash.spring.domain.User;
import ru.gulash.spring.repostory.UserRepository;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findUsersByName(String name) {
        return userRepository.findByName(name);
    }

    public List<User> findUsersOlderThan(int age) {
        return userRepository.findByAgeGreaterThan(age);
    }

    public List<User> findUsersByNameAndEmail(String name, String email) {
        return userRepository.findByNameAndEmail(name, email);
    }
}

