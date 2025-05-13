Проект для демонстрации по Spring Data Rest

# Spring Data REST Demo

Демонстрационное приложение, показывающее возможности Spring Data REST.

## Описание проекта

Этот проект демонстрирует использование Spring Data REST для автоматического создания REST API на основе репозиториев Spring Data JPA. 
Приложение представляет собой простую библиотечную систему с сущностями Author (Автор), Book (Книга) и Category (Категория).

### Основные возможности Spring Data REST

1. **Автоматическое создание REST API** — создает endpoints для CRUD-операций.
2. **Поддержка HATEOAS** — включает ссылки для навигации по API.
3. **Поддержка пагинации и сортировки** — параметры page, size и sort.
4. **Поддержка поиска** — автоматически создает endpoints для методов поиска.
5. **Поддержка проекций** — позволяет настраивать представление сущностей.
6. **Поддержка отношений между сущностями** — позволяет работать с отношениями через API.
7. **Настраиваемые базовые пути** — позволяет изменять пути для ресурсов.
8. **Настраиваемые контроллеры** — позволяет добавлять пользовательские endpoints.
9. **Поддержка валидации** — использует Jakarta Bean Validation.
10. **Поддержка HAL и других форматов** — предоставляет различные форматы представления.

## Технологии

- Java 21
- Spring Boot 3.3.4
- Spring Data JPA
- Spring Data REST
- Spring HATEOAS
- H2 Database (для разработки)
- PostgreSQL (для продакшена)
- Lombok
- OpenAPI (Swagger)
- Docker

## Структура проекта

```
src/main/java/org/gualsh/demo/spdrest/
├── SpringDataRestDemoApplication.java     # Главный класс приложения
├── config/                               # Конфигурационные классы
│   ├── JpaConfig.java                    # Конфигурация JPA
│   ├── OpenApiConfig.java                # Конфигурация OpenAPI
│   ├── SpringDataRestConfig.java         # Конфигурация Spring Data REST
│   └── DataInitializer.java              # Инициализация тестовых данных
├── controller/                           # Контроллеры
│   ├── CustomBookController.java         # Расширение функциональности REST API
│   └── HomeController.java               # Контроллер главной страницы
├── eventhandler/                         # Обработчики событий
│   └── BookEventHandler.java             # Обработчик событий для книг
├── model/                                # Модели данных
│   ├── Author.java                       # Модель автора
│   ├── Book.java                         # Модель книги
│   └── Category.java                     # Модель категории
├── projection/                           # Проекции для REST API
│   ├── AuthorWithBooks.java              # Проекция для автора с книгами
│   └── BookSummary.java                  # Проекция для книги
└── repository/                           # Репозитории
    ├── AuthorRepository.java             # Репозиторий для автора
    ├── BookRepository.java               # Репозиторий для книги
    └── CategoryRepository.java           # Репозиторий для категории
```

## Запуск приложения

### Запуск с использованием Maven

```bash
# Запуск с профилем разработки (H2 Database)
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Запуск с PostgreSQL
./mvnw spring-boot:run -Dspring.profiles.active=postgres
```

### Запуск с использованием Docker Compose

```bash
# Сборка и запуск контейнеров
docker-compose up -d

# Остановка контейнеров
docker-compose down

# Остановка контейнеров с удалением данных
docker-compose down -v
```

## Использование API

После запуска приложения API будет доступно по адресу: [http://localhost:8080/api](http://localhost:8080/api)

### Swagger UI

Документация API доступна по адресу: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### H2 Console

Консоль H2 (для профиля dev) доступна по адресу: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

### Примеры запросов

#### Базовые запросы CRUD

```bash
# Получение списка всех книг
curl -X GET http://localhost:8080/api/books

# Получение конкретной книги по ID
curl -X GET http://localhost:8080/api/books/1

# Создание новой книги
curl -X POST -H "Content-Type: application/json" -d '{
  "title": "Новая книга",
  "isbn": "1234567890123",
  "language": "Русский",
  "pageCount": 320,
  "publisher": "Издательство"
}' http://localhost:8080/api/books

# Обновление книги
curl -X PATCH -H "Content-Type: application/json" -d '{
  "title": "Обновленное название"
}' http://localhost:8080/api/books/1

# Удаление книги
curl -X DELETE http://localhost:8080/api/books/1
```

#### Запросы с пагинацией и сортировкой

```bash
# Пагинация: получение первой страницы книг (20 книг на страницу)
curl -X GET "http://localhost:8080/api/books?page=0&size=20"

# Сортировка: получение книг, отсортированных по названию
curl -X GET "http://localhost:8080/api/books?sort=title,asc"

# Комбинированный запрос: пагинация + сортировка
curl -X GET "http://localhost:8080/api/books?page=0&size=10&sort=publicationDate,desc"
```

#### Запросы с использованием методов поиска

```bash
# Поиск книг по названию
curl -X GET "http://localhost:8080/api/books/search/byTitle?title=чистый"

# Поиск книг по языку
curl -X GET "http://localhost:8080/api/books/search/byLanguage?language=Русский"

# Поиск книг по нескольким критериям
curl -X GET "http://localhost:8080/api/books/search/byMultipleCriteria?title=основание&language=Английский"
```

#### Запросы с использованием проекций

```bash
# Получение книг с использованием проекции BookSummary
curl -X GET "http://localhost:8080/api/books?projection=bookSummary"

# Получение авторов с использованием проекции AuthorWithBooks
curl -X GET "http://localhost:8080/api/authors?projection=authorWithBooks"
```

#### Кастомные запросы

```bash
# Получение статистики по языкам книг
curl -X GET http://localhost:8080/api/books/stats/by-language

# Фильтрация книг по нескольким параметрам
curl -X GET "http://localhost:8080/api/books/filter?language=Английский&minPages=400"

# Получение рекомендаций по книгам
curl -X GET http://localhost:8080/api/books/1/recommend
```