package ru.gulash.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.gulash.spring.domain.User;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    // Пример 1: Поиск пользователей по имени
    @Query("{ 'name': :#{#name} }")
    List<User> findByName(@Param("name") String name);

    // Пример 2: Поиск пользователей старше заданного возраста
    @Query("{ 'age': { '$gt': :#{#age} } }")
    List<User> findByAgeGreaterThan(@Param("age") int age);

    // Пример 3: Поиск пользователей по имени и email
    @Query("{ 'name': :#{#name}, 'email': :#{#email} }")
    List<User> findByNameAndEmail(@Param("name") String name, @Param("email") String email);
}

