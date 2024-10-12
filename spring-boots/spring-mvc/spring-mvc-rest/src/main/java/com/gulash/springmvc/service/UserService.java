package com.gulash.springmvc.service;

import com.gulash.springmvc.entity.User;
import com.gulash.springmvc.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service // Компонент выполняет ТОЛЬКО бизнес-логику т.е. НЕ ВЗАИМОДЕЙСТВУЕТ с БД, HTTP и т.д.
public class UserService {
    private final UserRepository userRepository;

    // Получаем на вход РЕПОЗИТОРИЙ
    public UserService(@Autowired UserRepository userRepository) { this.userRepository = userRepository; }

    // Проброс на нижний уровень.
    // 1 Реализуем по БИЗНЕС-ЛОГИКУ.
    // 2 Вызываем методы РЕПОЗИТОРИЯ.
    // 3 Может ограничить доступные операции(например без удаления)
    public User findUserById(Long id) { return userRepository.findUserById(id); }

    public User save(User toSave) { return userRepository.save(toSave); }

    public List<User> findAll() { return (List<User>) userRepository.findAll(); }
}
