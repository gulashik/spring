package ru.gulash.actuatordemo.init;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.gulash.actuatordemo.model.User;
import ru.gulash.actuatordemo.repository.UserRepository;

import java.util.List;

/**
 * Компонент для инициализации тестовых данных при запуске приложения.
 * CommandLineRunner запускается после инициализации контекста Spring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    /**
     * Метод запускается при старте приложения.
     *
     * @param args аргументы командной строки
     */
    @Override
    public void run(String... args) {
        log.info("Инициализация тестовых данных...");

        // Создаем тестовых пользователей если таблица пуста
        if (userRepository.count() == 0) {
            List<User> users = List.of(
                new User(null, "user1", "user1@example.com", true),
                new User(null, "user2", "user2@example.com", true),
                new User(null, "user3", "user3@example.com", false),
                new User(null, "admin", "admin@example.com", true)
            );

            userRepository.saveAll(users);
            log.info("Созданы {} тестовых пользователя", users.size());
        } else {
            log.info("Данные уже инициализированы, пропускаем...");
        }
    }
}
