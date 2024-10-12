package com.gulash.springmvc.repository;

import com.gulash.springmvc.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// СОЗДАЁМ Интерфейс-Репозиторий НАСЛЕДУЕМ его от CrudRepository<Entity, ID> ОБОГАЩАЕМ нужными действиями
// ДОП. ДЕЙСТВИЯ УКАЗЫВАЮТСЯ В СТОГО ОПРЕДЕЛЁННОМ ПОРЯДКЕ
// ДЕЙСТВИЯ ГЕНЕРИРУЮТСЯ при компиляции
@Repository // Необязательно, т.к. у наследников CrudRepository есть @Indexed, которая указывает, что наследники будут рассматриваться как бин-репозиторий
public interface UserRepository // Объявляем дополнительные методы не указанные в CrudRepository
        extends // CrudRepository - Объявлены методы методы взаимодействия с БД
            CrudRepository<User/*Класс-Entity*/, Long/*Тип ID*/> {
    User findUserById(Long id);
}
