package com.gulash.springmvc.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity // Сущность - Таблица
@Table(name = "users") // Имя таблицы. Возможен варианта со схемой @Table(name="CUST", schema="RECORDS")
public class User {

    // Ограничения
    // Класс НЕ может быть final и Поля класса НЕ могут содержать final-полей/свойств
    // Класс должен содержать хотя бы одно @Id-поле
    // Должен содержать "Конструктор по умолчанию", т.е. без параметров
    // Getters and Setters - для возможности конвертации в JSON

    @Id // Поле является ID Обязательное указание
    // @GeneratedValue(strategy=GenerationType.AUTO)
    // @GeneratedValue(strategy=SEQUENCE, generator="CUST_SEQ")
    private long id;

    @Column(name = "username") // Можно без указания тогда имя столбца будет как поля
    private String username;

    @Column(name = "firstName") // Можно без указания тогда имя столбца будет как поля
    private String firstName;

    @Column(name = "lastName") // Можно без указания тогда имя столбца будет как поля
    private String lastName;

    @DateTimeFormat( // конвертация в дату из входных параметров
            pattern = "MM-dd-yyyy", // Нужный паттерн конвертации
            fallbackPatterns = { "M/d/yy", "dd.MM.yyyy" } // Резервные паттерны конвертации
    )
    @Column(name = "hire_date") // Можно без указания тогда имя столбца будет как поля
    private LocalDate hireDate;

    // Конструктор по умолчанию для Spring - обязательно нужен если есть конструктор с параметрами
    // Модификатор доступа public или protected
    protected User() {}

    // Конструктор с инициализацией полей
    public User(long id, String username, String firstName, String lastName, LocalDate hireDate) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hireDate = hireDate;
    }

    // Getters and Setters - для полей(столбцов таблицы)
    // Вроде как можно только Getters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
