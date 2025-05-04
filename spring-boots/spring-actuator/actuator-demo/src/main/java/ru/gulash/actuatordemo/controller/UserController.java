package ru.gulash.actuatordemo.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gulash.actuatordemo.model.User;
import ru.gulash.actuatordemo.repository.UserRepository;

import java.util.List;

/**
 * REST-контроллер для работы с пользователями.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * Получение всех пользователей.
     * Метод аннотирован @Timed для автоматического создания метрики времени выполнения.
     *
     * @return список всех пользователей
     */
    @GetMapping
    @Timed("api.users.get.all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Получение пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return пользователь или 404 ошибка если не найден
     */
    @GetMapping("/{id}")
    @Timed("api.users.get.byId")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создание нового пользователя.
     *
     * @param user данные пользователя
     * @return созданный пользователь
     */
    @PostMapping
    @Timed("api.users.create")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    /**
     * Обновление пользователя.
     *
     * @param id идентификатор пользователя
     * @param updatedUser обновленные данные пользователя
     * @return обновленный пользователь или 404 ошибка если не найден
     */
    @PutMapping("/{id}")
    @Timed("api.users.update")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id)
            .map(user -> {
                user.setUsername(updatedUser.getUsername());
                user.setEmail(updatedUser.getEmail());
                user.setActive(updatedUser.isActive());
                return ResponseEntity.ok(userRepository.save(user));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удаление пользователя.
     *
     * @param id идентификатор пользователя
     * @return статус 204 No Content при успешном удалении
     */
    @DeleteMapping("/{id}")
    @Timed("api.users.delete")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Поиск пользователей по имени.
     *
     * @param username имя пользователя
     * @return список пользователей с указанным именем
     */
    @GetMapping("/search")
    @Timed("api.users.search")
    public List<User> searchUsers(@RequestParam String username) {
        return userRepository.findByUsername(username);
    }
}
