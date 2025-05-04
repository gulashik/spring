package ru.gulash.actuatordemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gulash.actuatordemo.model.User;

import java.util.List;

/**
 * Репозиторий для работы с сущностью User.
 * JpaRepository предоставляет базовые методы для работы с сущностью:
 * - save, saveAll
 * - findById, findAll
 * - delete, deleteById
 * и другие.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователей по имени пользователя.
     *
     * @param username имя пользователя
     * @return список пользователей с указанным именем
     */
    List<User> findByUsername(String username);

    /**
     * Находит пользователей по статусу активности.
     *
     * @param active статус активности
     * @return список пользователей с указанным статусом
     */
    List<User> findByActive(boolean active);

    /**
     * Находит пользователей по email.
     *
     * @param email email пользователя
     * @return пользователь с указанным email
     */
    User findByEmail(String email);
}
